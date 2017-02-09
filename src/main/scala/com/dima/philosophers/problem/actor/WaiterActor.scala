package com.dima.philosophers.problem.actor

import akka.actor.Actor
import akka.event.Logging

import scala.collection.mutable

/**
  * Waiter Actor implementation
  *
  * @author dmihalishin@gmail.com
  */
class WaiterActor(forks: Set[Fork]) extends Actor {
  val log = Logging(context.system, this)
  private val data = mutable.Map[Fork, Philosopher]()

  override def receive: Receive = {
    case TakeFork(philosopher, fork) => takeFork(philosopher, fork)
    case PutForkBack(fork) =>
      log.debug(s"Put Fork ${fork.number} back by ${sender().toString()}")
      data -= fork
    case _ => log.warning("Get unknown message")
  }

  private def takeFork(philosopher: Philosopher, fork: Fork) = {
    forks.find(_.number == fork.number) match {
      case Some(value) => data.get(value) match {
        case Some(usedBy) =>
          log.debug(s"Fork ${fork.number} used by ${usedBy.name}")
          sender() ! ForkUsed(fork)
        case None =>
          log.debug(s"Fork ${fork.number} taken by ${philosopher.name}")
          data(fork) = philosopher
          sender() ! ForkTaken(fork)
      }
      case None => sender() ! NotExistingFork(fork)
    }
  }
}

case class Fork(number: Int)

abstract class Action

case class TakeFork(philosopher: Philosopher, fork: Fork) extends Action

case class ForkTaken(fork: Fork) extends Action

case class PutForkBack(fork: Fork) extends Action

case class ForkUsed(fork: Fork) extends Action

case class NotExistingFork(fork: Fork) extends Action