package org.example.protocol;

import org.example.Client;
import org.example.network.Routable;
import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;


import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractTCP extends Routable implements TCP {

    private Logger logger = Logger.getLogger(Client.class.getName());

    //private Queue<Packet> inputBuffer;
    private Queue<Packet> outputBuffer;


    public AbstractTCP(int inputBufferSize, int outputBufferSize) {
        this.outputBuffer = new BufferQueue<>(outputBufferSize);
        //this.inputBuffer = new BufferQueue<>(inputBufferSize);
    }

    @Override
    public void connect() {

    }

    @Override
    public void send(Packet packet) {
        boolean wasAdded = this.outputBuffer.offer(packet);
        if (!wasAdded) {
            logger.log(Level.WARNING, "packet was not added to the output queue");
            return;
        }
    }

    @Override
    public Packet receive() {
        return this.dequeueInputBuffer();
    }

    @Override
    public void close() {

    }

    @Override
    public void run() {

    }


    /**
     * Layer 1
     */

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        return this.inputBuffer.offer(packet);
    }

    @Override
    public Packet dequeueInputBuffer() {
        return this.inputBuffer.poll();
    }

    @Override
    public boolean inputQueueIsEmpty() {
        return this.inputBuffer.isEmpty();
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
    public boolean outputQueueIsEmpty() {
        return this.outputBuffer.isEmpty();
    }
}
