package org.example.protocol.window;

import org.example.data.BufferQueue;
import org.example.data.Packet;

import java.util.concurrent.BlockingQueue;

public class BasicWindow implements Window {

    //todo - dette burde ligge direkte i bufferqueue

    private BlockingQueue<Packet> window;
    private int size;

    public BasicWindow(int windowSize) {
        this.window =  new BufferQueue(windowSize);
        this.size = windowSize;

    }

    @Override
    public boolean isWaiting() {
        return this.window.remainingCapacity() == 0;
    }

    @Override
    public void packetToAck(Packet packet) {
        if (isWaiting()) return;
        window.add(packet);
    }

    @Override
    public void receivedAck(Packet ack) {
        for (Packet packet : this.window) {
            if (packet.getSequenceNumber() == ack.getAcknowledgmentNumber()){
                this.window.remove(packet);
                return;
            }
        }

    }

    @Override
    public int windowSize() {
        return this.size;
    }
}
