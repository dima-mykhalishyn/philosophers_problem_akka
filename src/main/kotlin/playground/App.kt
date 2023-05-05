/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package playground

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import playground.actors.Philosopher
import playground.actors.Waiter

private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun main() = runBlocking {
    val forks = (1..5).map { Fork(it) }
    val channel = Channel<Action>(capacity = forks.size)
    val philosophers = listOf(
        Philosopher("P1", channel, forks[0], forks[1]),
        Philosopher("P2", channel, forks[1], forks[2]),
        Philosopher("P3", channel, forks[2], forks[3]),
        Philosopher("P4", channel, forks[3], forks[4]),
        Philosopher("P5", channel, forks[4], forks[0])
    )
    philosophers.forEachIndexed { index, philosopher ->
        channel.send(RequestFork(philosopher, forks[index]))
    }
    val lookup = philosophers.associateBy({ it }, { philosopherActor(it) })
    val waiter = Waiter(lookup)
    try {
        for (action in channel) {
            waiter.takeAnAction(action)
        }
    } finally {
        channel.close()
    }
}

@OptIn(ObsoleteCoroutinesApi::class)
fun philosopherActor(philosopher: Philosopher): SendChannel<Action> = coroutineScope.actor {
    for (action in channel) {
        philosopher.takeAnAction(action)
    }
}