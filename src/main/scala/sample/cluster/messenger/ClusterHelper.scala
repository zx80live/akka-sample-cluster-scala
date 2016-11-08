package sample.cluster.messenger

import akka.actor.{Actor, ActorRef, ActorSelection}
import akka.cluster.{Cluster, Member, MemberStatus}

import scala.collection.immutable.SortedSet

trait ClusterHelper {
  this: Actor =>

  val cluster: Cluster

  def selection(m: Member): ActorSelection

  def membersCount(role: String): Int = members(role).size

  def members(role: String): SortedSet[Member] =
    cluster.state.members.filter(m => m.status == MemberStatus.Up && m.hasRole(role))

  def memberSelectors(role: String): Set[ActorSelection] = members(role) map selection

  def broadcast(msg: Any, role: String, sender: ActorRef): Unit = memberSelectors(role).foreach(_.tell(msg, sender))

  def broadcast(msg: Any, role: String): Unit = broadcast(msg, role, self)
}
