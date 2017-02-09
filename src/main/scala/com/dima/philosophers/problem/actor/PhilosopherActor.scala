package com.dima.philosophers.problem.actor

import akka.actor.{Actor, ActorRef}
import akka.event.Logging

import scala.collection.mutable

/**
  * Philosopher Actor implementation
  *
  * @author dmihalishin@gmail.com
  */
class PhilosopherActor(waiter: ActorRef, philosopher: Philosopher, left: Fork, right: Fork) extends Actor {
  val log = Logging(context.system, this)
  private val status = mutable.Map(left -> false, right -> false)

  override def receive: Receive = {
    case ForkTaken(fork) =>
      log.debug(s"Fork ${fork.number} taken")
      status(fork) = true
      if (status.forall(_._2)) eat()
      takeNextFork()
    case ForkUsed(fork) =>
      log.debug(s"Fork used ${fork.number}")
      status.withFilter(_._2).foreach(data => {
        waiter ! PutForkBack(data._1)
        status(data._1) = false
      })
      Thread.sleep(100) // simulate small pause
      takeNextFork()
    case NotExistingFork(fork) => error("NotExistingFork: " + fork.number)
    case _ => log.warning("Get unknown message")
  }

  private def takeNextFork() = status.find(!_._2) match {
    case Some(lastFork) => waiter ! TakeFork(philosopher, lastFork._1)
    case None => error("Cannot find valid fork, logic is broken")
  }

  private def error(message: String): Unit = {
    log.error(message)
    context.stop(self)
  }

  private def eat(): Unit = {
    log.info(s"Eating with ${left.number}:${right.number}")
    Thread.sleep(2000) // simulate eating process :)
    status.foreach(data => status(data._1) = false)
    waiter ! PutForkBack(left)
    waiter ! PutForkBack(right)
    log.info("Finish eating")
    Thread.sleep(1000) // simulate pause after lunch :)
  }
}

case class Philosopher(name: String)