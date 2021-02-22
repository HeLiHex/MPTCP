package org.example.simulator.events;

import org.example.util.Util;

import java.util.Queue;

public abstract class Event implements Comparable<Event> {

    private static final int DEFAULT_DELAY = 1;
    private final int instant;

    public Event(int delay){
        this.instant = Util.getTime() + delay;
    }

    public Event(){
        this(DEFAULT_DELAY);
    }

    public abstract void run();

    public abstract void generateNextEvent(Queue<Event> events);

    public int getInitInstant(){
        return this.instant;
    }

    @Override
    public int compareTo(Event o) {
        if (this.getInitInstant() < o.getInitInstant()) return -1;
        if (this.getInitInstant() > o.getInitInstant()) return 1;
        return 0;
    }

}
