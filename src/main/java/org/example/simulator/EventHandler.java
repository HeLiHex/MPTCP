package org.example.simulator;

import org.example.simulator.events.Event;
import org.example.simulator.statistics.Statistics;
import org.example.util.Util;

import java.util.PriorityQueue;
import java.util.Queue;

public class EventHandler {

    private static final Statistics STATISTICS = new Statistics();
    private final Queue<Event> events;

    public EventHandler() {
        this.events = new PriorityQueue<>();
        Util.setSeed(1337);
        Util.resetTime();
        Statistics.reset();
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

    public void printStatistics() {
        //STATISTICS.createChart();
        System.out.println(STATISTICS.toString());
    }

    public boolean singleRun() {
        var event = this.events.poll();
        //System.out.println(event);
        if (event == null) {
            if (!this.events.isEmpty())
                throw new IllegalStateException("get null event when events queue are nonempty!");
            return false;
        }
        event.run();
        Util.tickTime(event);
        event.generateNextEvent(this.events);
        return true;
    }

    public void run() {
        while (singleRun()) ;
    }
}
