package org.example.util;

import org.example.data.Packet;

import java.time.Duration;
import java.time.Instant;

public class WaitingPacket implements Comparable<WaitingPacket>{

    private Packet packet;
    private Duration timeoutDuration;
    private Instant timeoutInstant;

    public WaitingPacket(Packet packet, Duration duration) {
        this.packet = packet;
        this.timeoutDuration = duration;
        this.timeoutInstant = Instant.now().plus(duration);
    }

    public void restart(){
        if (!timeoutFinished()) throw new IllegalStateException("can't restart unfinished timer");
        this.timeoutInstant = this.timeoutInstant.plus(timeoutDuration);
    }

    public boolean timeoutFinished(){
        return this.timeoutInstant.isBefore(Instant.now());
    }

    public Instant getTimeoutInstant() {
        return timeoutInstant;
    }

    public Packet getPacket() {
        return packet;
    }

    @Override
    public int compareTo(WaitingPacket o) {
        return this.getTimeoutInstant().compareTo(o.getTimeoutInstant());
    }

    @Override
    public String toString() {
        return packet.toString() + "[timeout: " + this.timeoutInstant + "]";
    }
}
