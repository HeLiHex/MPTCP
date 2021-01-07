package org.example.util;

import org.example.data.Packet;

public class WaitingPacket {

    private Packet packet;
    private int timeout;
    private final int TIMEOUT_DURATION;

    public WaitingPacket(Packet packet, int timeout) {
        this.packet = packet;
        this.TIMEOUT_DURATION = timeout;
        this.timeout = timeout;
    }

    public void restart(){
        if (!timeoutFinished()) throw new IllegalStateException("can't restart unfinished timer");
        this.timeout = TIMEOUT_DURATION;
    }

    public boolean timeoutFinished(){
        return timeout <= 0;
    }

    public void update(){
        if (timeoutFinished()) return;
        this.timeout--;
    }

    public Packet getPacket() {
        return packet;
    }

    @Override
    public String toString() {
        return packet.toString() + "[timeout: " + this.timeout + "]";
    }
}
