package sample.cluster.messenger

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import akka.routing.{BroadcastRoutingLogic, RoutingLogic}
import com.typesafe.config.ConfigFactory
import sample.cluster.messenger.AdminCommands.{Exit, _}
import sample.cluster.util.ConsoleCSS._

import scala.collection.immutable.SortedSet
import scala.util.{Failure, Success}

class AdminFrontend extends ClusterNode with ClusterHelper {

  override val nodeRole: String = "worker"

  override def routingLogic: RoutingLogic = BroadcastRoutingLogic()

  override val cluster: Cluster = Cluster(context.system)

  private var messagePeriod: Long = 10
  private var messageTimeout: Long = 100

  override val customEvents: Receive = {
    case StopEval =>
      evaluator.foreach(e => context.stop(e))
      evaluator = None
      self ! DeleteNode("all")

    case Hello =>
      broadcast("hello", nodeRole, self)

    case ViewNodes => onViewNodes()
    case DeleteNode(address) => onDeleteNode(address)
    case AddNode => onAddNode()
    case GetClusterStatistic => onClusterStatistic(sender())
    case NoClusterStatistic =>
      println(s"No statistic because cluster is empty".attr(Foreground.Yellow))

    case s@ClusterStatisticResult(xs) => onStatisticResult(s)
    case Eval => onEval()
    case SetInterval(p: Long, t: Long) =>
      messagePeriod = p
      messageTimeout = t

      broadcast(SetPeriod(p, t), nodeRole)
      println(s"set interval ${(p, t)}")

    case e =>
      println(s"Event $e".attr(Foreground.DarkGray))
  }

  def onViewNodes(): Unit = {
    val xs: SortedSet[Member] = members(nodeRole)
    if (xs.nonEmpty)
      xs.zipWithIndex
        .map(m => s"[${m._2}] ${m._1.uniqueAddress.address.toString}/user/$nodeRole".attr(Foreground.Green)) foreach println
    else
      println("Cluster is empty".attr(Foreground.Green))
  }

  def onDeleteNode(address: String): Unit = {
    val p = """([0-9\s]+)""".r
    val nodes = address match {
      case p(numbers) =>
        val indexes = numbers.split("\\s+").map(_.toInt)

        memberSelectors(nodeRole).zipWithIndex.collect {
          case (a, i) if indexes.contains(i) => a
        }

      case "all" =>
        memberSelectors(nodeRole)

      case _ => List(context.actorSelection(address))
    }

    println(s" select for del $nodes".attr(Foreground.Red))
    nodes.foreach(_ ! RemoveSelfMember)
  }

  def onAddNode(): Unit = {
    //TODO log to file
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=0").
      withFallback(ConfigFactory.parseString(s"akka.cluster.roles = [$nodeRole]")).
      withFallback(ConfigFactory.parseString("akka.loglevel = OFF")).
      withFallback(ConfigFactory.parseString("akka.stdout-loglevel=OFF")).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props(classOf[NodeWorker], messagePeriod, messageTimeout), nodeRole)

    sender() ! NodeAdded
  }

  def onClusterStatistic(consumer: ActorRef): Unit = {
    membersCount("worker") match {
      case 0 =>

        consumer ! NoClusterStatistic
      case count =>
        val aggregator = context.actorOf(Props(classOf[StatisticAggregator], count, consumer))
        broadcast(GetStatistic, nodeRole, aggregator)
    }
  }


  def onStatisticResult(s: ClusterStatisticResult): Unit = {
    println(s.xs.map(e => s"$e".attr(Foreground.Yellow)).mkString("\n"))
    println(s"avg: ${s.averageCount}, max: ${s.maxCount}, min: ${s.minCount}, during the last ${s.period} ms, timeout: ${s.timeout}".attr(Foreground.Yellow))
  }

  def onEval(): Unit = {
    val a = context.system.actorOf(Props(new Evaluator(self)), name = "eval")
    evaluator = Some(a)
  }

  var evaluator: Option[ActorRef] = None
}


object AdminFrontend {
  def main(args: Array[String]): Unit = {
    val port = args.headOption.getOrElse("0")
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.parseString("akka.loglevel = OFF")).
      withFallback(ConfigFactory.parseString("akka.stdout-loglevel = OFF")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val admin = system.actorOf(Props[AdminFrontend], name = "frontend")

    var ok = true
    while (ok) {
      println("Enter command:".attr(Foreground.Green))
      AdminCommands.parse(scala.io.StdIn.readLine()) match {
        case Success(Exit) =>
          admin ! Exit
          system.terminate()
          ok = false
        case Success(EmptyCommand) =>
        case Success(cmd) => admin.tell(cmd, admin)
        case Failure(e) =>
          println(e.getMessage.attr(Foreground.Red))

      }
    }
  }
}