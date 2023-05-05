package playground.actors

import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging
import playground.*

/**
 * Waiter actor implementation
 *
 * @author dmihalishin@gmail.com
 */
class Waiter(
    private val lookup: Map<Philosopher, SendChannel<Action>>
) {
    private val logger = KotlinLogging.logger("Waiter")
    private val status: MutableMap<Fork, Philosopher?> = mutableMapOf()

    suspend fun takeAnAction(action: Action) = when (action) {
        is RequestFork -> forkAllocation(action.philosopher, action.fork)
        is PutForkBack -> makeForkAvailable(action.fork)
        else -> logger.warn { "Unexpected action: $action" }
    }

    private suspend fun forkAllocation(philosopher: Philosopher, fork: Fork) {
        val usedBy = status[fork]
        if (usedBy == null) {
            logger.debug { "Fork ${fork.number} allocated by ${philosopher.name}" }
            status[fork] = philosopher
            lookup[philosopher]?.send(ForkTaken(fork))
        } else {
            logger.debug { "${philosopher.name}. Fork ${fork.number} already used by ${usedBy.name}" }
            lookup[philosopher]?.send(ForkUsed(fork, usedBy.name))
        }
    }

    private fun makeForkAvailable(fork: Fork) {
        logger.debug { "Fork Available: ${fork.number}" }
        status[fork] = null
    }
}