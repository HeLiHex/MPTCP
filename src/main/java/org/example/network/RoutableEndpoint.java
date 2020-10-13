package org.example.network;

import org.example.data.BufferQueue;
import org.example.data.Packet;

import java.util.Random;

public abstract class RoutableEndpoint extends Routable implements Endpoint{

    private BufferQueue<Packet> outputBuffer;

    public RoutableEndpoint(BufferQueue<Packet> inputBuffer, BufferQueue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
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
