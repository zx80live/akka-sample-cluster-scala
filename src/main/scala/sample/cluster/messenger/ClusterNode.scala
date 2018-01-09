package sample.cluster.messenger

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, RootActorPath}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import akka.routing.{ActorRefRoutee, Routee, Router, RoutingLogic}
import sample.cluster.util.ConsoleCSS.{Foreground, _}

import scala.collection.immutable.IndexedSeq

abstract class ClusterNode extends Actor with ActorLogging {

  val cluster: Cluster
  val nodeRole: String

  def routingLogic: RoutingLogic

  val customEvents: Receive

  private var router = Router(routingLogic, Vector.empty[ActorRefRoutee])

  final val receiveClusterEvents: Receive = {
    case RemoveSelfMember =>
      cluster.leave(self.path.address)
      context.stop(self)
      log.info("Handled RemoveSelfMember {}".attr(Foreground.Red), self)
      onSelfMemberRemoved()

    case MemberRemoved(m, _) =>
      val a = selection(m)
      router = router.removeRoutee(a)
      onMemberRemoved(m)
      log.info("Member removed {}".attr(Foreground.Red), m)

    case MemberUp(m: Member) if m.hasRole(nodeRole) =>
      if (!isSelf(m)) {
        val a = selection(m)
        router = router.removeRoutee(a).addRoutee(a)
        onMemberUp(m)
        log.info("Register member {}".attr(Foreground.Blue), m)
      }

    case e: MemberEvent =>
      log.info(Console.BLUE + s"cluster event: $e" + Console.RESET)
  }

  def onSelfMemberRemoved(): Unit = {}

  def onMemberUp(m: Member): Unit = {}

  def onMemberRemoved(m: Member): Unit = {}

  def route(msg: Any, sender: ActorRef): Unit = router.route(msg, sender)

  def routees: IndexedSeq[Routee] = router.routees

  def isEmptyRoutees: Boolean = routees.isEmpty

  override def preStart(): Unit =
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])

  override def postStop(): Unit = cluster.unsubscribe(self)

  final override def receive: Receive = receiveClusterEvents orElse customEvents

  def selection(m: Member): ActorSelection = context.actorSelection(RootActorPath(m.address) / "user" / nodeRole)

  def address(a: ActorRef): String = {
    val Array(addr, _) = akka.serialization.Serialization.serializedActorPath(a).split("#")
    addr
  }

  def address(m: Member): String = selection(m).toSerializationFormat

  def isSelf(m: Member): Boolean = address(m) == address(self)

}

