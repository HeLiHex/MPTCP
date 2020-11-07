package org.example.protocol;

import org.example.data.BufferQueue;
import org.example.data.Packet;

import java.util.Random;

public class BasicTCP extends AbstractTCP {

    private static final int BUFFER_SIZE = 20;
    private static final double NOISE_TOLERANCE = 100.0;
    private boolean waitingForACK;

    public BasicTCP(Random randomGenerator) {
        super(new BufferQueue<Packet>(BUFFER_SIZE), new BufferQueue<Packet>(BUFFER_SIZE), randomGenerator, NOISE_TOLERANCE);
        this.waitingForACK = false;
    }

    @Override
    protected boolean isWaitingForACK() {
        return this.waitingForACK;
    }

    @Override
    protected void releaseWaitForAck() {
        this.waitingForACK = false;
    }

    @Override
    protected void setWaitForAck() {
        this.waitingForACK = true;

    }
}
