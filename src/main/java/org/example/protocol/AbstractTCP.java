package org.example.protocol;

import org.example.network.Routable;
import org.example.data.BufferQueue;
import org.example.data.Packet;


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractTCP extends Routable implements TCP {

    private Logger logger = Logger.getLogger(AbstractTCP.class.getName());

    public AbstractTCP(BufferQueue<Packet> inputBuffer, BufferQueue<Packet> outputBuffer, Random randomGenerator) {
        super(inputBuffer, outputBuffer, randomGenerator);
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
        while (true){
            if (!this.inputQueueIsEmpty()){
                Packet packet = this.receive();
                System.out.println("endpunkt har pakken: " + packet);
            }
        }
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
