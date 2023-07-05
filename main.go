package main

import (
	"fmt"
	"runtime"
	"sync"
)

func main() {
	runtime.GOMAXPROCS(4)

	// waiter
	waiterChannel := make(chan Action)
	var group sync.WaitGroup
	group.Add(1)
	channels := map[string]chan Action{}
	waiter := Waiter{channels: channels, forks: map[Fork]bool{}}
	go func(wg *sync.WaitGroup) {
		defer wg.Done()
		for a := range waiterChannel {
			waiter.TakeAnAction(a)
			if len(waiter.channels) == 0 {
				close(waiterChannel)
			}
		}
	}(&group)

	// philosophers
	for i := 0; i < 5; i++ {
		philosopher := Philosopher{
			Name:          fmt.Sprintf("P_%d", i),
			leftFork:      Fork(i % 5),
			rightFork:     Fork((i + 1) % 5),
			leftHand:      NoFork,
			rightHand:     NoFork,
			waiterChannel: waiterChannel,
			eatCounter:    2,
		}
		channel := make(chan Action)
		channels[philosopher.Name] = channel
		go func(p Philosopher, c chan Action) {
			for a := range c {
				p.TakeAnAction(a)
				if p.eatCounter == 0 {
					close(c)
				}
			}
		}(philosopher, channel)
		waiterChannel <- Action{value: RequestFork, fork: philosopher.leftFork, sender: philosopher.Name}
	}

	group.Wait()
}
