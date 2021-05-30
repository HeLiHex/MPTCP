package org.example.simulator;

import org.example.simulator.events.Event;
import org.example.util.Util;

import java.util.PriorityQueue;
import java.util.Queue;

public class EventHandler {

    private final Queue<Event> events;

    public EventHandler() {
        this.events = new PriorityQueue<>();
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }

    public Event peekEvent() {
        return this.events.peek();
    }

    public Queue<Event> getEvents() {
        return events;
    }

    public int getNumberOfEvents() {
        return this.events.size();
    }

    public boolean singleRun() {
        var event = this.events.poll();
        if (event == null) {
            if (!this.events.isEmpty())
                throw new IllegalStateException("get null event when events queue are nonempty!");
            return false;
        }
        Util.tickTime(event);
        event.run();
        event.generateNextEvent(this.events);
        return true;
    }

    public void run() {
        while (singleRun()) ;
    }
}
