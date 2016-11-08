package sample.cluster.messenger

import scala.util.{Failure, Success, Try}

object AdminCommands {

  trait AdminCommand

  case class DeleteNode(address: String) extends AdminCommand

  case object AddNode extends AdminCommand

  case object NodeAdded extends AdminCommand

  case object StopEval extends AdminCommand

  case object Exit extends AdminCommand

  case object ViewNodes extends AdminCommand

  case object GetClusterStatistic extends AdminCommand

  case object NoClusterStatistic extends AdminCommand

  case object EmptyCommand extends AdminCommand

  case object Help extends AdminCommand

  case object Eval extends AdminCommand

  case object Hello extends AdminCommand

  case class SetInterval(period: Long, timeout: Long) extends AdminCommand

  private val delPattern = """del\s+([\d+\w+-\\#@.:\s]+)""".r
  private val intervalPattern = """int\s+(\d+)\s+(\d+)""".r

  def parse(cmd: String): Try[AdminCommand] = cmd match {
    case delPattern(a) => Success(DeleteNode(a))
    case intervalPattern(period, timeout) =>
      for {
        p <- Try(period.toInt)
        t <- Try(timeout.toInt)
        if p <= t
      } yield SetInterval(p, t)

    case "nodes" | "list" | "info" => Success(ViewNodes)
    case "stat" => Success(GetClusterStatistic)
    case "add" => Success(AddNode)
    case "help" => Success(Help)
    case "eval" => Success(Eval)
    case "stopEval" | "stop" => Success(StopEval)
    case "hello" => Success(Hello)
    case "exit" | "quit" | "e" | "q" => Success(Exit)
    case c if c.isEmpty => Success(EmptyCommand)
    case unknown => Failure(new IllegalArgumentException(s"$unknown: command not found. Use help command."))
  }
}