package org.example.simulator.events;

import java.time.Instant;
import java.util.Queue;

public abstract class Event implements Comparable<Event> {

    private final double time;

    public Event(double time){
        this.time = time;
    }

    public abstract void run();

    public abstract void generateNextEvent(Queue<Event> events);

    public double getInitTime(){
        return this.time;
    }

    public static long getCurTime(){
        return Instant.now().getNano();

    }

    @Override
    public int compareTo(Event o) {
        if (o.getInitTime() > this.getInitTime()) return -1;
        if (o.getInitTime() < this.getInitTime()) return 1;
        return 0;
    }

}
