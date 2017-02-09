package com.dima.philosophers.problem.actor

import akka.actor.{ActorSystem, Props, Terminated}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Tests for PhilosopherActor
  *
  * @author dmihalishin@gmail.com
  */
class PhilosopherActorTest extends TestKit(ActorSystem(
  "test",
  ConfigFactory.parseString(
    """akka {
         loglevel = "DEBUG"
       }
    """)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val phil = Philosopher("A")
  val (left, right) = (Fork(1), Fork(2))
  val philosopher = system.actorOf(Props(classOf[PhilosopherActor], self, phil, left, right), "PhilosopherA")

  override def afterAll {
    Await.result(system.terminate(), 10 seconds)
    shutdown()
  }

  "Philosopher" should {
    "try to take right Fork if left already taken" in {
      within(1000 millis) {
        philosopher ! ForkTaken(left)
        expectMsg(TakeFork(phil, right))
      }
    }
    "start eating if both forks taken, and put Forks back after" in {
      within(5000 millis) {
        philosopher ! ForkTaken(right)
        expectMsg(PutForkBack(left))
        expectMsg(PutForkBack(right))
        expectMsg(TakeFork(phil, left))
      }
    }
  }

  "Philosopher" should {
    "try to take left Fork if right already taken" in {
      within(1000 millis) {
        philosopher ! ForkTaken(right)
        expectMsg(TakeFork(phil, left))
      }
    }
    "put right Fork back if he cannot take left Fork" in {
      within(1000 millis) {
        philosopher ! ForkUsed(left)
        expectMsg(PutForkBack(right))
        expectMsg(TakeFork(phil, left))
      }
    }
  }

  "Philosopher" should {
    "ignore unknown messages" in {
      within(1000 millis) {
        philosopher ! "ignore"
        expectNoMsg()
      }
    }
    "stop self" in {
      within(1000 millis) {
        val testProbe = TestProbe()
        testProbe watch philosopher
        philosopher ! NotExistingFork(Fork(15))
        val msg = testProbe.expectMsgType[Terminated]
        assert(msg.actor.path.name == "PhilosopherA")
      }
    }
  }

}
