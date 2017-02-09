package com.dima.philosophers.problem.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Tests for WaiterActor
  *
  * @author dmihalishin@gmail.com
  */
class WaiterActorTest extends TestKit(ActorSystem(
  "test",
  ConfigFactory.parseString(
    """akka {
         loglevel = "DEBUG"
       }
    """)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val (left, right) = (Fork(1), Fork(2))
  var philA = Philosopher("A")
  val philB = Philosopher("B")
  val waiter = system.actorOf(Props(classOf[WaiterActor], Set(left, right)), "waiter")

  override def afterAll {
    Await.result(system.terminate(), 10 seconds)
    shutdown()
  }

  "Philosopher A" should {
    "take left Fork" in {
      within(1000 millis) {
        waiter ! TakeFork(philA, left)
        expectMsg(ForkTaken(left))
      }
    }
    "take right Fork" in {
      within(1000 millis) {
        waiter ! TakeFork(philA, right)
        expectMsg(ForkTaken(right))
      }
    }
  }

  "Philosopher B" should {
    "not take left Fork" in {
      within(1000 millis) {
        waiter ! TakeFork(philB, left)
        expectMsg(ForkUsed(left))
      }
    }
    "not take right Fork" in {
      within(1000 millis) {
        waiter ! TakeFork(philB, right)
        expectMsg(ForkUsed(right))
      }
    }
    "take right Fork if it will be free" in {
      within(1000 millis) {
        waiter ! PutForkBack(right)
        waiter ! TakeFork(philB, right)
        expectMsg(ForkTaken(right))
      }
    }
  }
  "Waiter" should {
    "ignore unknown message" in {
      within(1000 millis) {
        waiter ! "ignore"
        expectNoMsg()
      }
    }
  }
}
