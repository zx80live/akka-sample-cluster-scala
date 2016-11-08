package sample.cluster.messenger

import akka.actor.{Actor, ActorLogging, ActorRef, ReceiveTimeout}

import scala.concurrent.duration._

class StatisticAggregator(expectedResults: Int, replyTo: ActorRef) extends Actor with ActorLogging {
  var results = Vector.empty[Statistic]
  context.setReceiveTimeout(3.seconds)

  override def receive = {
    case s: Statistic =>
      results = results :+ s
      if (results.size == expectedResults) {
        replyTo ! ClusterStatisticResult(results)
        context.stop(self)
      }
    case ReceiveTimeout =>
      replyTo ! FailedResponse("Service unavailable, try again later")
      context.stop(self)
  }

}
