package org.example.network;

import org.example.data.BufferQueue;
import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoutableEndpoint extends Routable implements Endpoint {

    protected BlockingQueue<Packet> outputBuffer;
    private BlockingQueue<Packet> receivedPackets;

    protected RoutableEndpoint(BlockingQueue<Packet> inputBuffer, BlockingQueue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
        super(inputBuffer, randomGenerator, noiseTolerance);
        this.outputBuffer = outputBuffer;
        this.receivedPackets = new BufferQueue<>(100);
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

    public Packet getReceivedPacket(){
        return this.receivedPackets.poll();
    }

    @Override
    public void run(){
        if (this.inputBufferIsEmpty()) return;
        Packet received = this.dequeueInputBuffer();
        receivedPackets.add(received);
        Logger.getLogger(this.getName()).log(Level.INFO, () -> "Packet: " + received + " received");
    }
}
