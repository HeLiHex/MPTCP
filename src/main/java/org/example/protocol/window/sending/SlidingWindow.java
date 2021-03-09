package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.util.BoundedPriorityBlockingQueue;
import org.example.util.BoundedQueue;

import java.util.Comparator;


public class SlidingWindow extends Window implements SendingWindow, BoundedQueue<Packet> {

    private final BoundedQueue<Packet> window;
    private static final int DEFAULT_CONGESTION_WINDOW_SIZE = 1;
    private final int receiverWindowSize;
    private int dupAckCount = 0;

    public SlidingWindow(int receiverWindowSize, Connection connection, Comparator comparator) {
        super(1000000, connection, comparator);
        this.window = new BoundedPriorityBlockingQueue<>(DEFAULT_CONGESTION_WINDOW_SIZE, comparator);
        this.receiverWindowSize = receiverWindowSize;
    }

    @Override
    public boolean isWaitingForAck() {
        return this.window.isFull();
    }

    @Override
    public void ackReceived(Packet ack) {
        int ackIndex = this.sendingPacketIndex(ack);

        for (int i = 0; i <= ackIndex; i++) {
            this.window.poll();
            this.increase();
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
        if (this.window.contains(packet)){
            if (this.sendingPacketIndex(packet) == 0) this.decrease();
            return true;
        }
        return false;
    }

    @Override
    public void increase() {
        if (this.getWindowSize() >= this.receiverWindowSize) return;
        this.window.resize(this.getWindowSize() + 1);
    }

    @Override
    public void decrease() {
        int newWindowSize = Math.max((int) Math.ceil(this.getWindowSize() / 2), DEFAULT_CONGESTION_WINDOW_SIZE);
        for (int i = 0; i < this.getWindowSize() - newWindowSize; i++) {
            this.offer(this.window.poll());
        }
        this.window.resize(newWindowSize);
    }

    @Override
    public int getWindowSize() {
        return this.window.size();
    }
}
