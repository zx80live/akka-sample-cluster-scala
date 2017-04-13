package sample.cluster.util

import scala.language.postfixOps

/**
  * TODO doc
  *
  * @param periodInMillis TODO doc
  */
case class MessageCounter(periodInMillis: Long = 1000, statistic: Vector[Long] = Vector.empty[Long]) {
  type TimeMillis = Long

  @volatile private var stat: Vector[TimeMillis] = statistic

  def register(): Unit = {
    val current = System.currentTimeMillis()
    val withoutOld = stat.drop(stat.indexWhere(_ >= current - periodInMillis))
    stat = withoutOld :+ current
  }

  def count: Long = stat.length

  def setPeriod(valueInMillis: Long): MessageCounter =
    if (valueInMillis > 0)
      MessageCounter(valueInMillis, stat)
    else
      throw new IllegalArgumentException(s"Illegal value for period in millis $valueInMillis")
}
