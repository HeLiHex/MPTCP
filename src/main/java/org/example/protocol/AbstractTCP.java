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

    private Queue<Packet> inputBuffer;
    private Queue<Packet> outputBuffer;


    public AbstractTCP(int inputBufferSize, int outputBufferSize) {
        this.outputBuffer = new BufferQueue<>(outputBufferSize);
        this.inputBuffer = new BufferQueue<>(inputBufferSize);
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
        return this.inputBuffer.poll();
    }

    @Override
    public void close() {

    }

    public Queue<Packet> getOutputBuffer() {
        return this.outputBuffer;
    }

    @Override
    public void run() {

    }
}
