package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.util.BoundedPriorityBlockingQueue;
import org.example.util.BoundedQueue;

import java.util.Comparator;


public class SlidingWindow extends Window implements SendingWindow, BoundedQueue<Packet> {

    private final BoundedQueue<Packet> window;
    private final int defaultCongestionSize;

    public SlidingWindow(int windowSize, Connection connection, Comparator comparator) {
        super(1000000, connection, comparator);
        this.window = new BoundedPriorityBlockingQueue<>(windowSize, comparator);
        this.defaultCongestionSize = 1;
    }

    @Override
    public boolean isWaitingForAck() {
        return this.window.isFull();
    }

    @Override
    public void ackReceived(Packet ack) {
        int ackIndex = this.sendingPacketIndex(ack);
        for (int i = 0; i <= ackIndex; i++) {
            System.out.println("poll");
            this.window.poll();
        }
        this.connection.update(ack);
    }

    @Override
    public Packet send() {
        Packet packetToSend = this.poll();
        assert packetToSend != null : "packet to send is null";
        if (this.window.offer(packetToSend)){
            return packetToSend;
        }
        throw new IllegalStateException("Packet was not added to the sending window");
    }

    @Override
    public boolean canRetransmit(Packet packet) {
        return this.window.contains(packet);
    }

    @Override
    public void increase() {
        this.window.resize(this.getWindowSize() + 1);
    }

    @Override
    public void decrease() {
        int curWindowSize = this.getWindowSize();
        int newWindowSize = (int)Math.ceil(curWindowSize/2);
        if (newWindowSize == 0) newWindowSize = this.defaultCongestionSize;

        for (int i = 0; i < curWindowSize - newWindowSize; i++) {
            this.offer(this.window.poll());
        }
        this.window.resize(newWindowSize);
    }
}
