package org.example.simulator.simulatables;

import org.example.simulator.Statistics;
import org.example.util.Util;

import java.util.PriorityQueue;
import java.util.Queue;

public class SimulatableEventHandler {

    private static final Statistics STATISTICS = new Statistics();
    private final Queue<SimulatableEvent> simulatableEvents;

    public SimulatableEventHandler() {
        this.simulatableEvents = new PriorityQueue<>();
        Util.setSeed(1337);
        Util.resetTime();
        Statistics.reset();
    }

    public void addEvent(SimulatableEvent simulatableEvent) {
        this.simulatableEvents.add(simulatableEvent);
    }

    public SimulatableEvent peekEvent() {
        return this.simulatableEvents.peek();
    }

    public Queue<SimulatableEvent> getEvents() {
        return this.simulatableEvents;
    }

    public int getNumberOfEvents() {
        return this.simulatableEvents.size();
    }

    public void printStatistics() {
        //STATISTICS.createChart();
        System.out.println(STATISTICS.toString());
    }

    public boolean singleRun() {
        SimulatableEvent simulatableEvent = this.simulatableEvents.poll();
        if (simulatableEvent == null) {
            if (!this.simulatableEvents.isEmpty())
                throw new IllegalStateException("get null event when events queue are nonempty!");
            return false;
        }
        simulatableEvent.run();
        Util.tickTime(simulatableEvent);
        simulatableEvent.generateNextEvent(this.simulatableEvents);
        return true;
    }

    public void run() {
        while (singleRun()) ;
    }
}
