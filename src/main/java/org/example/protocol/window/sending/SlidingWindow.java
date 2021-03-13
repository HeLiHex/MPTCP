package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.data.Payload;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.util.BoundedQueue;

import java.util.*;
import java.util.logging.Level;

public class SlidingWindow extends Window implements SendingWindow, BoundedQueue<Packet> {

    private final List<Payload> payloadsToSend;
    private static final int DEFAULT_CONGESTION_WINDOW_CAPACITY = 1;
    private final int receiverWindowSize;
    private boolean seriousLossDetected = false;

    public SlidingWindow(int receiverWindowCapacity, Connection connection, Comparator<Packet> comparator, List<Payload> payloadsToSend) {
        super(DEFAULT_CONGESTION_WINDOW_CAPACITY, connection, comparator);
        this.payloadsToSend = payloadsToSend;
        this.receiverWindowSize = receiverWindowCapacity;
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
            this.seriousLossDetected = false;
        }
        this.connection.update(ack);
    }

    @Override
    public Packet send() {
        int nextPacketSeqNum = this.connection.getNextSequenceNumber() + this.size();
        Packet packet = new PacketBuilder()
                .withConnection(this.connection)
                .withPayload(this.payloadsToSend.remove(0))
                .withSequenceNumber(nextPacketSeqNum)
                .build();

        if (super.offer(packet)){
            return packet;
        }
        return null;

        /*
        Packet packetToSend = this.payloadsToSend.remove(0);
        assert packetToSend != null : "packet to send is null";
        if (super.offer(packetToSend)){
            return packetToSend;
        }
        throw new IllegalStateException("Packet was not added to the sending window");

         */
    }



    @Override
    public boolean canRetransmit(Packet packet) {
        if (this.contains(packet)){
            if (this.sendingPacketIndex(packet) == 0) this.decrease();
            if (this.sendingPacketIndex(packet) >= this.getWindowCapacity() - 1) this.seriousLossDetected = true;
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
        int newWindowSize = Math.max((int) (this.getWindowCapacity() / 2.0), DEFAULT_CONGESTION_WINDOW_CAPACITY);
        this.setBound(newWindowSize);
    }

    @Override
    public boolean isSeriousLossDetected(){
        return this.seriousLossDetected;
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
        return this.payloadsToSend.add(packet.getPayload());
    }

    @Override
    public int queueSize(){
       return this.payloadsToSend.size();
    }

    @Override
    public boolean isQueueEmpty(){
        return this.payloadsToSend.isEmpty();
    }


}
