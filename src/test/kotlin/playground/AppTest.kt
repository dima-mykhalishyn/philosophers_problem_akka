package playground

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import playground.actors.Philosopher
import playground.actors.Waiter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that validate`Dining philosophers problem` logic
 *
 * @author dmihalishin@gmail.com
 */
class AppTest {

    @Test
    fun `No hungry philosophers expected`() = runTest {
        val forks = (1..5).map { Fork(it) }
        val channel = Channel<Action>(capacity = forks.size)
        val summary = mutableSetOf<String>()
        val philosophers = listOf(
            Philosopher("P1", channel, forks[0], forks[1]) { summary.add(it.name) },
            Philosopher("P2", channel, forks[1], forks[2]) { summary.add(it.name) },
            Philosopher("P3", channel, forks[2], forks[3]) { summary.add(it.name) },
            Philosopher("P4", channel, forks[3], forks[4]) { summary.add(it.name) },
            Philosopher("P5", channel, forks[4], forks[0]) { summary.add(it.name) }
        )
        philosophers.forEachIndexed { index, philosopher ->
            channel.send(RequestFork(philosopher, forks[index]))
        }
        val lookup = philosophers.associateBy({ it }, { philosopherActor(it) })
        val waiter = Waiter(lookup)
        try {
            repeat(100) {
                waiter.takeAnAction(channel.receive())
            }
        } finally {
            channel.close()
        }
        assertEquals(5, summary.size, "Expected that all philosophers will eat at least ones")
    }
}
