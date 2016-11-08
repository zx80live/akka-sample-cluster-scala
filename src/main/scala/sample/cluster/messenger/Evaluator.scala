package sample.cluster.messenger

import akka.actor.{Actor, ActorRef}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.util.Timeout
import sample.cluster.messenger.AdminCommands._
import sample.cluster.util.ConsoleCSS._

import scala.concurrent.duration._

class Evaluator(admin: ActorRef) extends Actor {

  import context.dispatcher

  val cluster: Cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  override def postStop(): Unit = cluster.unsubscribe(self)

  context.system.scheduler.scheduleOnce(1.seconds)(requestNewNode)

  def requestNewNode():Unit = {
    admin.tell(AddNode, self)
    println(s"Evaluator: request new node".attr(Foreground.Blue))
  }

  override def receive = {
    case MemberUp(m) if m.hasRole("worker") =>
      context.system.scheduler.scheduleOnce(1.seconds, self, "wait..")
      admin.tell(GetClusterStatistic, self)

    case StopEval =>
      println(" -- Stop eval".attr(Foreground.LightBlue))

    case NoClusterStatistic =>
      println(" -- NoClusterStatistic".attr(Foreground.DarkGray))

    case s@ClusterStatisticResult(xs) =>
      println(s" -- nodes=${s.nodes}, avg=${s.averageCount}, min=${s.minCount}, max=${s.maxCount}, p=${s.period}, timeout=${s.timeout}".attr(Foreground.Yellow))
      requestNewNode()
  }
}
