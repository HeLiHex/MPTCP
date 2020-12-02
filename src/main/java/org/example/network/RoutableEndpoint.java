package org.example.network;

import org.example.data.BufferQueue;
import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;

import java.util.Queue;
import java.util.Random;

public abstract class RoutableEndpoint extends Routable implements Endpoint {

    private volatile Queue<Packet> outputBuffer;

    public RoutableEndpoint(Queue<Packet> inputBuffer, Queue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
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
    public abstract void run();
}
