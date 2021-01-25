package org.example.simulator.events.run;

import org.example.network.interfaces.Endpoint;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;


public class RunEndpointEvent extends Event {


    private final Endpoint endpoint;

    public RunEndpointEvent(Instant instant, Endpoint endpoint) {
        super(instant);
        this.endpoint = endpoint;
    }

    public RunEndpointEvent(Endpoint endpoint) {
        super();
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        this.endpoint.run();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {

    }

}
