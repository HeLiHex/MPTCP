package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public abstract class RoutableEndpoint extends Routable implements Endpoint {

    protected BlockingQueue<Packet> outputBuffer;

    protected RoutableEndpoint(BlockingQueue<Packet> inputBuffer, BlockingQueue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
        super(inputBuffer, randomGenerator, noiseTolerance);
        this.outputBuffer = outputBuffer;
    }

    @Override
    public Packet dequeueOutputBuffer() {
        return this.outputBuffer.poll();
    }

    @Override
    public boolean enqueueOutputBuffer(Packet packet) {
        return this.outputBuffer.offer(packet);
    }

    @Override
    public boolean outputBufferIsEmpty() {
        return this.outputBuffer.isEmpty();
    }

    @Override
    public int outputBufferRemainingCapacity() {
        return this.outputBuffer.remainingCapacity();
    }

    @Override
    public String toString() {
        return "Endpoint: " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public abstract void run();
}
