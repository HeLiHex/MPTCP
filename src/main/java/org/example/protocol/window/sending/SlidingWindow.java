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
        if (this.getWindowCapacity() >= this.receiverWindowSize) return;
        this.window.resize(this.getWindowCapacity() + 1);
    }

    @Override
    public void decrease() {
        int newWindowSize = Math.max((int) Math.ceil(this.getWindowCapacity() / 2), DEFAULT_CONGESTION_WINDOW_SIZE);
        for (int i = 0; i < this.getWindowCapacity() - newWindowSize; i++) {
            this.offer(this.window.poll());
        }
        this.window.resize(newWindowSize);
    }

    @Override
    public int packetsInTransmission(){
        return this.window.size();
    }


    @Override
    public int getWindowCapacity() {
        return this.window.size();
        //return this.window.bound();
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

}
