package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoutableEndpoint extends Routable implements Endpoint {

    protected final BlockingQueue<Packet> outputBuffer;
    private final BlockingQueue<Packet> receivedPackets;

    public RoutableEndpoint(BlockingQueue<Packet> inputBuffer, BlockingQueue<Packet> outputBuffer, double noiseTolerance) {
        super(inputBuffer, noiseTolerance);
        this.outputBuffer = outputBuffer;
        this.receivedPackets = new ArrayBlockingQueue<>(10000);
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
    public int outputBufferSize() {
        return this.outputBuffer.size();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    public Packet getReceivedPacket() {
        return this.receivedPackets.poll();
    }

    @Override
    public void run() {
        if (this.inputBufferIsEmpty()) return;
        Packet received = this.dequeueInputBuffer();
        this.receivedPackets.add(received);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, () -> "Packet: " + received + " received");
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

}
