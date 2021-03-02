package org.example.protocol;

import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.simulator.Statistics;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger;

    private Packet lastAcknowledged;
    private int dupAckCount;

    private static final int WINDOW_SIZE = 4;
    private static final int BUFFER_SIZE = 10000;
    private static final double NOISE_TOLERANCE = 100.0;
    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);

    private final BlockingQueue<Packet> received;
    private final BoundedPriorityBlockingQueue<Packet> waitingPackets;
    //private final BlockingQueue<Packet> receivedAck;

    public BasicTCP() {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                NOISE_TOLERANCE
        );
        this.logger = Logger.getLogger(this.getClass().getSimpleName());

        this.dupAckCount = 0;

        this.received = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.waitingPackets = new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR);
        //this.receivedAck = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
    }

    private void addToReceived(Packet packet){
        if (this.received.contains(packet)) return;
        boolean added = this.received.offer(packet);
        if (!added) throw new IllegalStateException("Packet was not added to the received queue");
        Statistics.packetReceived();
    }

    /*
    private void addToReceivedAck(Packet packet){
        boolean added = this.receivedAck.offer(packet);
        if (!added) throw new IllegalStateException(" Packet was not added to the receivedAck queue");
    }

     */

    @Override
    public Packet receive() {
        return this.received.poll();
    }

    private void ack(Packet packet){
        this.lastAcknowledged = packet;
        Packet ack = new PacketBuilder().ackBuild(packet);
        this.route(ack);
    }

    private void dupAck(){
        if (this.lastAcknowledged == null) return;
        this.ack(this.lastAcknowledged);
    }

    @Override
    protected int setReceived() {
        Packet packet = this.dequeueInputBuffer();
        if (packet == null) throw new IllegalStateException("null packet received");
        if (this.inReceivingWindow(packet)){
            if (receivingPacketIndex(packet) == 0){
                this.updateConnection(packet);
                this.ack(packet);
                this.addToReceived(packet);
                return 1;
            }
            //todo - packet should be received
            //to implement DupACK this packet should be received if it's not already received.
            //the problem is that just adding will result inn multiple packets
            if (!received.contains(packet)){
                this.dupAck();
                this.addToReceived(packet);
            }
            return 1;
        }
        return 0;
    }
/*
    public boolean hasPacketsToRetransmit(){
        for (Packet p : this.waitingPackets){
            boolean ackNotReceivedOnPacket = !this.receivedAck.contains(p);
            boolean noMatchingWaitingPacketOnAck = this.receivedAck.stream().noneMatch((packet -> packet.getAcknowledgmentNumber() - 1 == p.getSequenceNumber()));

            boolean packetShouldBeRetransmitted = ackNotReceivedOnPacket && noMatchingWaitingPacketOnAck;
            if (packetShouldBeRetransmitted) return true;
        }
        return false;
    }

 */


    public boolean packetIsWaiting(Packet packetToMatch){
        /*
        boolean ackNotReceivedOnPacket = !this.receivedAck.contains(packetToMatch);
        boolean noMatchingWaitingPacketOnAck = this.receivedAck.stream().noneMatch((packet -> packet.getAcknowledgmentNumber() - 1 == packetToMatch.getSequenceNumber()));
        return ackNotReceivedOnPacket && noMatchingWaitingPacketOnAck;
         */
        return waitingPackets.contains(packetToMatch);
    }


    @Override
    public Packet[] packetsToRetransmit() {
        /*
        Queue<Packet> retransmit = new PriorityQueue<>(PACKET_COMPARATOR);
        for (Packet p : this.waitingPackets) {
            boolean ackNotReceivedOnPacket = !this.receivedAck.contains(p);
            boolean noMatchingWaitingPacketOnAck = this.receivedAck.stream().noneMatch((packet -> packet.getAcknowledgmentNumber() - 1 == p.getSequenceNumber()));
            if (ackNotReceivedOnPacket && noMatchingWaitingPacketOnAck){
                boolean added = retransmit.offer(p);
                if (!added) throw new IllegalStateException("a packet was not added to the retransmit queue");
            }
        }

        return retransmit.toArray(new Packet[retransmit.size()]);

         */
        return null;
    }

    @Override
    public boolean isWaitingForACK() {
        return this.waitingPackets.isFull();
    }

    @Override
    protected void addToWaitingPacketWindow(Packet packet){
        boolean added = this.waitingPackets.offer(packet);
        if (!added) throw new IllegalStateException("Packet was not added to the waitingPackets queue");
    }

    @Override
    protected void ackReceived() {
        Packet ack = this.dequeueInputBuffer();
        if (!this.isConnected()){
            logger.log(Level.INFO, "ack received with no connection established");
            return;
        }

        int ackIndex = sendingPacketIndex(ack);
        for (int i = 0; i <= ackIndex; i++) {
            waitingPackets.poll();
            this.dupAckCount = 0;
        }
        this.updateConnection(ack);
    }


    @Override
    public int getWindowSize() {
        return WINDOW_SIZE;
    }

    @Override
    public boolean inSendingWindow(Packet packet){
        int packetIndex = sendingPacketIndex(packet);
        return inWindow(packetIndex);
    }

    @Override
    protected boolean inReceivingWindow(Packet packet){
        int packetIndex = receivingPacketIndex(packet);
        return inWindow(packetIndex);
    }

    private boolean inWindow(int packetIndex){
        int windowSize = this.getWindowSize();
        return packetIndex < windowSize && packetIndex >= 0;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
