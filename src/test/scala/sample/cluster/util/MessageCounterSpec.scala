package sample.cluster.util

import org.scalatest.FunSuite
import org.scalatest.Matchers
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class MessageCounterSpec extends FunSuite with Matchers {

  val HALF_MINUTE: Long = 1000 * 30
  val period = 1000 * 10


  test("MessageCounter") {
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

    val result: List[MessageCounter] = Await.result(f, 1.minute)
    result.map(_.count) shouldEqual List(10, 20, 5)
  }
}
