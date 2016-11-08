package sample.cluster.messenger

trait MessengerEvent

case class Message(text: String) extends MessengerEvent

case object GetStatistic extends MessengerEvent

case class Statistic(address: String, count: Long, periodInMillis: Long, timeout: Long) extends MessengerEvent {
  override def toString: String = s"$count during the last $periodInMillis ms, timeout=$timeout ms for $address"
}

case object AddMember extends MessengerEvent

case object RemoveSelfMember extends MessengerEvent

case class SetPeriod(period: Long, timeout: Long) extends MessengerEvent

case object GetNodes extends MessengerEvent

case class ClusterStatisticResult(xs: Vector[Statistic]) extends MessengerEvent {
  lazy val (averageCount: Long, minCount: Long, maxCount: Long, nodes: Int, period: Long, timeout: Long) =
    if (xs.nonEmpty) {
      (xs.map(_.count).sum / xs.length,
        xs.map(_.count).min,
        xs.map(_.count).max,
        xs.length,
        xs.head.periodInMillis,
        xs.head.timeout)
    } else {
      (0L, 0L, 0L, 0, 0L, 0L)
    }
}

case class FailedResponse(text: String) extends MessengerEvent