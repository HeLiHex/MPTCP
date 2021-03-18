package org.example.simulator.events;

public abstract class EventGenerator extends Event {

    public EventGenerator(long delay) {
        super(delay);
    }

    @Override
    public void run() {
        //EventGenerators should only generate new events and not run
    }
}
