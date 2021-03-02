package org.example.simulator.events;

import org.example.util.Util;

import java.util.Queue;

public abstract class Event implements Comparable<Event> {

    private static final int DEFAULT_DELAY = 1;
    private final long instant;

    protected Event(long delay){
        if (delay < DEFAULT_DELAY) delay = DEFAULT_DELAY;
        this.instant = Util.getTime() + delay;
    }

    protected Event(){
        this(DEFAULT_DELAY);
    }

    public abstract void run();

    public abstract void generateNextEvent(Queue<Event> events);

    public long getInitInstant(){
        return this.instant;
    }

    @Override
    public int compareTo(Event o) {
        if (this.getInitInstant() < o.getInitInstant()) return -1;
        if (this.getInitInstant() > o.getInitInstant()) return 1;
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        //(Event.compareTo(e) == 0) != Event.Equals(e)
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
