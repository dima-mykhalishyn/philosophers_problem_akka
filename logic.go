package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

const (
	RequestFork = iota
	ForkTaken
	PutForkBack
	ForkUsed
	DinnerIsDone
)
const NoFork = -1

type Fork int

type Action struct {
	value  int
	fork   Fork
	sender string
}

type Waiter struct {
	channels map[string]chan Action
	forks    map[Fork]bool
	lock     sync.Mutex
}
type Philosopher struct {
	Name          string
	waiterChannel chan<- Action
	leftFork      Fork
	rightFork     Fork
	leftHand      Fork
	rightHand     Fork
	eatCounter    int
}

func (w *Waiter) TakeAnAction(a Action) {
	switch a.value {
	case RequestFork:
		fmt.Printf("Fork %d requested by %s \n", a.fork, a.sender)
		defer w.lock.Unlock()
		w.lock.Lock()
		available, ok := w.forks[a.fork]
		if available || !ok {
			if c, ok := w.channels[a.sender]; ok {
				fmt.Printf("Fork %d provided for %s \n", a.fork, a.sender)
				w.forks[a.fork] = false
				c <- Action{value: ForkTaken, fork: a.fork}
			}
		} else if c, ok := w.channels[a.sender]; ok {
			fmt.Printf("Fork %d already used. Cannot be taken by %s \n", a.fork, a.sender)
			c <- Action{value: ForkUsed, fork: a.fork}
		}
	case PutForkBack:
		fmt.Printf("Fork %d is available \n", a.fork)
		defer w.lock.Unlock()
		w.lock.Lock()
		w.forks[a.fork] = true
	case DinnerIsDone:
		fmt.Printf("Dinner is done for %s \n", a.sender)
		delete(w.channels, a.sender)
	default:
		fmt.Printf("Unexpected action %d \n", a.value)
	}
}

func (p *Philosopher) TakeAnAction(a Action) {
	switch a.value {
	case ForkTaken:
		p.takeFork(a.fork)
	case ForkUsed:
		fmt.Printf("Fork used %d. Putting forks back \n", a.fork)
		if p.leftHand != NoFork {
			p.waiterChannel <- Action{value: PutForkBack, fork: p.leftHand, sender: p.Name}
			p.leftHand = NoFork
		}
		if p.rightHand != NoFork {
			p.waiterChannel <- Action{value: PutForkBack, fork: p.rightHand, sender: p.Name}
			p.rightHand = NoFork
		}
		// ask again
		time.Sleep(1 * time.Second)
		p.waiterChannel <- Action{value: RequestFork, fork: p.leftFork, sender: p.Name}
	default:
		fmt.Printf("Unexpected action %d \n", a.value)
	}
}

func (p *Philosopher) takeFork(f Fork) {
	if p.leftFork == f {
		fmt.Printf("%s left hand took fork %d \n", p.Name, f)
		p.leftHand = f
		p.waiterChannel <- Action{value: RequestFork, fork: p.rightFork, sender: p.Name}
	} else if p.rightFork == f {
		fmt.Printf("%s right hand took fork %d \n", p.Name, f)
		p.rightHand = f
	} else {
		fmt.Printf("%s unexpected fork %d \n", p.Name, f)
	}
	if p.leftHand != NoFork && p.rightHand != NoFork {
		p.eat()
		// put forks back
		p.waiterChannel <- Action{value: PutForkBack, fork: p.leftFork, sender: p.Name}
		p.waiterChannel <- Action{value: PutForkBack, fork: p.rightFork, sender: p.Name}
		p.leftHand = NoFork
		p.rightHand = NoFork
		if p.eatCounter > 0 {
			// pause and ask for fork again
			time.Sleep(10 * time.Second)
			p.waiterChannel <- Action{value: RequestFork, fork: p.leftFork, sender: p.Name}
		} else {
			// dinner is done
			p.waiterChannel <- Action{value: DinnerIsDone, fork: p.leftFork, sender: p.Name}
		}
	}
}

func (p *Philosopher) eat() {
	fmt.Printf("%s is eating \n", p.Name)
	time.Sleep(time.Duration(rand.Intn(5)) * time.Second)
	p.eatCounter--
}
