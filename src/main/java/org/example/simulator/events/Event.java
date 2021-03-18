package org.example.simulator.events;

import org.example.network.Channel;
import org.example.network.interfaces.NetworkNode;
import org.example.util.Util;

import java.util.Queue;

public abstract class Event implements Comparable<Event> {

    private final long instant;

    protected Event(long delay) {
        this.instant = this.findInstant(delay);
    }

    protected Event() {
        this.instant = findInstant(0);
    }

    protected Event(NetworkNode node) {
        if (node == null) throw new IllegalArgumentException("node is null");
        this.instant = this.findInstant(node.processingDelay());
    }

    protected Event(Channel channel) {
        if (channel == null) throw new IllegalArgumentException("channel is null");
        this.instant = this.findInstant(channel.propogationDelay());
    }

    private long findInstant(long delay){
        return Util.getTime() + delay;
    }

    public abstract void run();

    public abstract void generateNextEvent(Queue<Event> events);

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
}
