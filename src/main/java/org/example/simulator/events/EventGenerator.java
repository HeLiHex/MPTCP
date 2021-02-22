package org.example.simulator.events;

public abstract class EventGenerator extends Event{

    public EventGenerator(int delay) {
        super(delay);
    }

    public EventGenerator() {
        super();
    }

    @Override
    public void run() {
        //EventGenerators should only generate new events and not run
    }
}
