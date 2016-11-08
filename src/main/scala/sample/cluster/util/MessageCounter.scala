package sample.cluster.util

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
  * TODO doc
  *
  * @param periodInMillis TODO doc
  */
case class MessageCounter(periodInMillis: Long = 1000, statistic: Vector[Long] = Vector.empty[Long]) {
  type TimeMillis = Long

  private var stat: Vector[TimeMillis] = statistic

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

//TODO move to test
object test extends App {

  private val HALF_MINUTE: Long = 1000 * 30
  val period = 1000 * 10

  val f1 = Future {
    val messages = 10
    val delay = period / messages
    val c = MessageCounter(period)

    (0 to 100).foreach(_ => c.register())
    Thread.sleep(period)

    (0 until messages) foreach { _ =>
      Thread.sleep(delay)
      c.register()
    }
    c
  }

  val f2 = Future {
    val messages = 20
    val delay = period / messages
    val c = MessageCounter(period)

    (0 to 100).foreach(_ => c.register())
    Thread.sleep(period)

    (0 until messages) foreach { _ =>
      Thread.sleep(delay)
      c.register()
    }
    c
  }

  val f3 = Future {
    val messages = 5
    val delay = period / messages
    val c = MessageCounter(period)

    (0 to 100).foreach(_ => c.register())
    Thread.sleep(period)

    (0 until messages) foreach { _ =>
      Thread.sleep(delay)
      c.register()
    }
    c
  }

  val f = Future.sequence(List(f1, f2, f3))

  private val result: List[MessageCounter] = Await.result(f, 1 minute)
  result.map(_.count) foreach println
}