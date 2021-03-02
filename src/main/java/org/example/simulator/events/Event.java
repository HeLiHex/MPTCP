package org.example.simulator.events;

import org.example.util.Util;

import java.util.Queue;

public abstract class Event implements Comparable<Event> {

    private final long instant;

    protected Event(long delay){
        this.instant = Util.getTime() + delay;
    }

    protected Event(){
        this(0);
    }

    public abstract void run();

    public abstract void generateNextEvent(Queue<Event> events);

    public long getInstant(){
        return this.instant;
    }

    @Override
    public int compareTo(Event o) {
        if (this.getInstant() < o.getInstant()) return -1;
        if (this.getInstant() > o.getInstant()) return 1;
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
