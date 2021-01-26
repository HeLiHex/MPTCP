package org.example.simulator.events;

import java.time.Instant;

public abstract class EventGenerator extends Event{

    public EventGenerator(Instant instant) {
        super(instant);
    }

    public EventGenerator() {
        super();
    }

    @Override
    public void run() {
        //EventGenerators should only generate new events and not run
    }
}
