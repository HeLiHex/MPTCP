package org.example.simulator.events;

import java.time.Instant;
import java.util.Queue;

public abstract class Event implements Comparable<Event> {

    private final Instant instant;

    public Event(Instant instant){
        this.instant = instant;
    }

    public abstract void run();

    public abstract void generateNextEvent(Queue<Event> events);

    public Instant getInitInstant(){
        return this.instant;
    }

    @Override
    public int compareTo(Event o) {
        return this.instant.compareTo(o.getInitInstant());
    }

}
