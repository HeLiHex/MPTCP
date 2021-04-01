package org.example.simulator.simulatables;

import org.example.simulator.events.Event;
import org.example.util.Util;

import java.util.Queue;

public abstract class SimulatableEvent implements Comparable<Event> {

    private final long instant;
    private final Simulatable simulatable;

    protected SimulatableEvent(Simulatable simulatable) {
        this.instant = this.findInstant(simulatable.delay());
        this.simulatable = simulatable;
    }

    public abstract void run();

    public void generateNextEvent(Queue<SimulatableEvent> simulatableEvents){
        for (Simulatable s : this.simulatable.simulatablesToEnqueue()) {
            simulatableEvents.add(new RunSimulatableEvent(s));
        }
    }

    protected Simulatable getSimulatable(){
        return this.simulatable;
    }

    private long findInstant(long delay) {
        return Util.getTime() + delay;
    }

    public long getInstant() {
        return this.instant;
    }

    @Override
    public int compareTo(Event o) {
        return Long.compare(this.getInstant(), o.getInstant());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
