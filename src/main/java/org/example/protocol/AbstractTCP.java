package org.example.protocol;

import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;

import java.util.Queue;

public class AbstractTCP implements TCP{

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
        this.outputBuffer.add(packet);
    }

    @Override
    public Packet receive() {

        return null;
    }

    @Override
    public void close() {

    }

    public Queue<Packet> getOutputBuffer() {
        return this.outputBuffer;
    }
}
