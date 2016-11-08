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

  context.system.scheduler.scheduleOnce(1.seconds)(requestNewNode())

  var statistics: Vector[ClusterStatisticResult] = Vector.empty[ClusterStatisticResult]
  var prevAvg: Option[Double] = None

  implicit class ListExt(l: Vector[ClusterStatisticResult]) {
    def avg: Double =
      if (l.nonEmpty)
        l.map(_.averageCount).sum / l.size
      else 0.0

    def add(s: ClusterStatisticResult, limit: Int = 3): Vector[ClusterStatisticResult] =
      if (l.size >= limit)
        l.drop(1) :+ s
      else
        l :+ s
  }

  def requestNewNode(): Unit = {
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

      statistics = statistics.add(s, 3)
      val newAvg = statistics.avg
      println(s" -- nodes=${s.nodes}, currAvg=${s.averageCount}, predictAvg=$newAvg, min=${s.minCount}, max=${s.maxCount}, during the last ${s.period} ms, timeout=${s.timeout}".attr(Foreground.Yellow))

      prevAvg = prevAvg match {
        case Some(avg) if avg <= newAvg =>
          requestNewNode()
          Some(newAvg)

        case Some(_) =>
          println(s"Optimal config: nodes=${s.nodes}, predictAvg=$newAvg, min=${s.minCount}, max=${s.maxCount}, during the last ${s.period} ms, timeout=${s.timeout}".attr(Background.Yellow, Foreground.Black))
          admin ! StopEval
          context.stop(self)
          None
        case None =>
          requestNewNode()
          Some(newAvg)
      }
  }
}
