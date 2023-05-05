package playground

import playground.actors.Philosopher

data class Fork(val number: Int)

sealed interface Action

data class RequestFork(val philosopher: Philosopher, val fork: Fork) : Action

data class ForkTaken(val fork: Fork) : Action

data class PutForkBack(val fork: Fork) : Action

data class ForkUsed(val fork: Fork, val philosopherName: String) : Action