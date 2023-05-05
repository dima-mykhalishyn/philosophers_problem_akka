package playground.actors

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import mu.KotlinLogging
import playground.*
import kotlin.random.Random

/**
 * Philosopher actor implementation
 *
 * @author dmihalishin@gmail.com
 */
class Philosopher(
    val name: String,
    private val waiterChannel: SendChannel<Action>,
    private val leftHand: Fork,
    private val rightHand: Fork,
    private val eatCallBack: (Philosopher) -> Unit = {}
) {
    private val logger = KotlinLogging.logger("Philosopher-$name")
    private var status = Pair(false, false)

    suspend fun takeAnAction(action: Action) = when (action) {
        is ForkTaken -> takeFork(action.fork)
        is ForkUsed -> {
            logger.debug { "Fork ${action.fork.number} occupied. Putting forks back." }
            putForksBack()
            delay(Random.nextLong(100, 500))
            askForFork()
        }

        else -> logger.warn { "Unexpected action: $action for ${name}" }
    }

    private suspend fun askForFork() = waiterChannel.send(
        RequestFork(
            this,
            if (status.first) rightHand else leftHand
        )
    )

    private suspend fun takeFork(fork: Fork) {
        if (leftHand == fork) {
            status = Pair(true, status.second)
            logger.debug { "$name left hand take the fork. [${status.first}, ${status.second}]" }
            askForFork()
        } else if (rightHand == fork) {
            status = Pair(status.first, true)
            logger.debug { "$name right hand take the fork. [${status.first}, ${status.second}]" }
        }
        if (status.first && status.second) {
            eat()
            delay(5000)
            askForFork()
        } else {
            logger.warn {
                "Handle fork ${fork.number}. " +
                        "$name status [${leftHand.number}: ${status.first}, ${rightHand.number}: ${status.second}]. "
            }
        }
    }

    private suspend fun eat() {
        logger.info { "$name eating [${leftHand.number}, ${rightHand.number}]" }
        eatCallBack(this)
        delay(Random.nextLong(1000, 2000))
        putForksBack()
    }

    private suspend fun putForksBack() {
        if (status.second)
            waiterChannel.send(PutForkBack(rightHand))
        if (status.first)
            waiterChannel.send(PutForkBack(leftHand))
        status = Pair(false, false)
    }
}