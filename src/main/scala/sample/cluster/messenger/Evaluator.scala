package sample.cluster.messenger

import akka.actor.{ActorRef, Cancellable}
import akka.cluster.Cluster
import akka.routing.{BroadcastRoutingLogic, RoutingLogic}
import akka.util.Timeout

import scala.concurrent.duration._

class Evaluator(admin: ActorRef) extends ClusterNode with ClusterHelper {

  import context.dispatcher

  override val cluster: Cluster = Cluster(context.system)
  override val nodeRole: String = "worker"

  override def routingLogic: RoutingLogic = BroadcastRoutingLogic()

  private var tickTask = {
    val periodTime = 1500.millisecond

    context.system.scheduler.schedule(periodTime, periodTime) {
      implicit val t = Timeout(2000.millisecond)

      admin ! AddMember
    }
  }

  override val customEvents: Receive = {

    case _ =>

  }
}
