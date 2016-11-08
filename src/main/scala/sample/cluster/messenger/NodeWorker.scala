package sample.cluster.messenger

import akka.actor.{ActorSystem, Cancellable, Props}
import akka.cluster.Cluster
import akka.routing._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import sample.cluster.util.ConsoleCSS._
import sample.cluster.util.MessageCounter

import scala.concurrent.duration._

class NodeWorker(messagePeriod: Long, messageTimeout: Long) extends ClusterNode {

  import context.dispatcher

  override val nodeRole: String = "worker"

  override def routingLogic: RoutingLogic = BroadcastRoutingLogic()

  override val cluster = Cluster(context.system)
  private val statistic = MessageCounter(periodInMillis = 1000)

  private var period: Long = messagePeriod
  private var timeoutMs: Long = messageTimeout
  private var tickTask = startTickTask()

  private def startTickTask(): Cancellable = {
    val periodTime = period.millisecond

    context.system.scheduler.schedule(periodTime, periodTime) {
      implicit val t = Timeout(timeoutMs.millisecond)
      if (!isEmptyRoutees) {
        route(Message("hello"), self)
      }
    }
  }

  override val customEvents: Receive = {
    case SetPeriod(p, t) =>
      tickTask.cancel()
      period = p
      timeoutMs = t
      tickTask = startTickTask()
      log.info(s"int ${(p, t)}".attr(Foreground.Cyan))

    case GetStatistic =>
      sender() ! Statistic(address(self), statistic.count, statistic.periodInMillis, timeoutMs)

    case Message(text) =>
      statistic.register()
      log.info("Handled {}".attr(Foreground.Green), statistic.count)

    case msg =>
      log.info("Handle {}".attr(Foreground.DarkGray), msg)
  }

  override def onSelfMemberRemoved(): Unit = tickTask.cancel()
}


object NodeWorker {
  def main(args: Array[String]): Unit = {
    val port = args.headOption.getOrElse("0")
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [worker]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props(classOf[NodeWorker], 10L, 100L), name = "worker")
  }
}