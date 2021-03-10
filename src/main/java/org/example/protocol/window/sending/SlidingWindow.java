package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.util.BoundedQueue;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class SlidingWindow extends Window implements SendingWindow, BoundedQueue<Packet> {

    private final Queue<Packet> queue;
    private static final int DEFAULT_CONGESTION_WINDOW_SIZE = 1;
    private final int receiverWindowSize;

    public SlidingWindow(int receiverWindowSize, Connection connection, Comparator comparator) {
        super(DEFAULT_CONGESTION_WINDOW_SIZE, connection, comparator);
        this.queue = new PriorityQueue<>(comparator);
        this.receiverWindowSize = receiverWindowSize;
    }

    @Override
    public boolean isWaitingForAck() {
        return this.isFull();
    }

    @Override
    public void ackReceived(Packet ack) {
        int ackIndex = this.sendingPacketIndex(ack);

        boolean dupAck = -this.getWindowCapacity() < ackIndex && ackIndex < 0;
        if (dupAck){
            return;
        }

        for (int i = 0; i <= ackIndex; i++) {
            this.poll();
            this.increase();
        }
        this.connection.update(ack);
    }

    @Override
    public Packet send() {
        Packet packetToSend = this.queue.poll();
        assert packetToSend != null : "packet to send is null";
        if (super.offer(packetToSend)){
            return packetToSend;
        }
        throw new IllegalStateException("Packet was not added to the sending window");
    }

    @Override
    public boolean canRetransmit(Packet packet) {
        if (this.contains(packet)){
            if (this.sendingPacketIndex(packet) == 0) this.decrease();
            return true;
        }
        return false;
    }

    @Override
    public void increase() {
        if (this.getWindowCapacity() >= this.receiverWindowSize) return;
        this.setBound(this.getWindowCapacity() + 1);
    }

    @Override
    public void decrease() {
        int newWindowSize = Math.max((int) Math.ceil(this.getWindowCapacity() / 2), DEFAULT_CONGESTION_WINDOW_SIZE);
        this.setBound(newWindowSize);
    }

    @Override
    public int packetsInTransmission(){
        return this.size();
    }

    @Override
    public boolean inSendingWindow(Packet packet){
        int packetIndex = sendingPacketIndex(packet);
        return inWindow(packetIndex);
    }

    @Override
    public int sendingPacketIndex(Packet packet){
        int packetSeqNum = packet.getSequenceNumber();
        int connSeqNum = this.connection.getNextSequenceNumber();
        return packetSeqNum - connSeqNum;
    }

    @Override
    public boolean offer(Packet packet) {
        return this.queue.offer(packet);
    }

    @Override
    public int queueSize(){
       return this.queue.size();
    }

    @Override
    public boolean isQueueEmpty(){
        return this.queue.isEmpty();
    }


}
