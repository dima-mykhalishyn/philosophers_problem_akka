package com.dima.philosophers.problem

import akka.actor.{ActorSystem, Props}
import com.dima.philosophers.problem.actor._

/**
  * Starter.
  * We have 5 Philosopher that sit at the round table with 5 Forks.
  *
  * @author dmihalishin@gmail.com
  */
object Application extends App {
  val system = ActorSystem("mySystem")
  val philosophers = for (i <- 1 to 5) yield Philosopher(i.toString)
  val forks = for (
    i <- 1 to philosophers.size
  ) yield Fork(i)
  val waiter = system.actorOf(Props(classOf[WaiterActor], forks.toSet), "waiter")
  for (
    (p, i) <- philosophers.zipWithIndex
  ) yield {
    val (l, r) = if (i + 1 < philosophers.size) (forks(i), forks(i + 1)) else (forks(i), forks(0))
    val sender = system.actorOf(Props(classOf[PhilosopherActor], waiter, p, l, r), "Philosopher" + (i + 1))
    waiter.tell(TakeFork(p, l), sender)
  }
}
