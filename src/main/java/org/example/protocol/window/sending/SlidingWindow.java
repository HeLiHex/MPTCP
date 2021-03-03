package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.util.BoundedPriorityBlockingQueue;
import org.example.util.BoundedQueue;


public class SlidingWindow extends Window implements SendingWindow, BoundedQueue<Packet> {

    private final BoundedQueue<Packet> window;

    public SlidingWindow(int windowSize, Connection connection) {
        super(windowSize, connection);
        this.window = new BoundedPriorityBlockingQueue<>(windowSize, PACKET_COMPARATOR);
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
        if (this.window.offer(packetToSend)){
            return packetToSend;
        }
        throw new IllegalStateException("Packet was not added to the sending window");
    }

    @Override
    public boolean canRetransmit(Packet packet) {
        return this.window.contains(packet);
    }
}
