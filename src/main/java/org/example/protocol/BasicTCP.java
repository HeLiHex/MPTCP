package org.example.protocol;

import org.example.data.Packet;
import org.example.util.BoundedPriorityBlockingQueue;
import org.example.util.WaitingPacket;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger;

    private static final int WINDOW_SIZE = 4;
    private static final int BUFFER_SIZE = 10000;
    private static final double NOISE_TOLERANCE = 100.0;
    private static final Duration TIMEOUT_DURATION = Duration.ofMillis(1000);
    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);

    private final BlockingQueue<Packet> received;
    private final BoundedPriorityBlockingQueue<WaitingPacket> waitingPackets;
    private final BlockingQueue<Packet> receivedAck;

    private Packet lastReceivedPacket = null;

    public BasicTCP(Random randomGenerator) {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                randomGenerator,
                NOISE_TOLERANCE
        );
        this.logger = Logger.getLogger(this.getName());

        this.received = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.waitingPackets = new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, Comparator.comparingInt(wp -> wp.getPacket().getSequenceNumber()));
        this.receivedAck = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
    }

    private void addToReceived(Packet packet){
        boolean added = this.received.offer(packet);
        if (!added) throw new IllegalStateException("Packet was not added to the received queue");
    }

    private void addToWaitingPackets(WaitingPacket waitingPacket){
        boolean added = this.waitingPackets.offer(waitingPacket);
        if (!added) throw new IllegalStateException("Packet was not added to the waitingPackets queue");
    }

    private void addToReceivedAck(Packet packet){
        boolean added = this.receivedAck.offer(packet);
        if (!added) throw new IllegalStateException(" Packet was not added to the receivedAck queue");
    }


    @Override
    public Packet receive() {
        return this.received.poll();
        /*if (received.isEmpty()) return null;

        if (this.lastReceivedPacket == null){
            Packet receivedPacket = this.received.poll();
            this.lastReceivedPacket = receivedPacket;
            return receivedPacket;
        }

        if (this.lastReceivedPacket.getSequenceNumber() + 1 == this.received.peek().getSequenceNumber()){
            Packet receivedPacket = this.received.poll();
            System.out.println(receivedPacket.getSequenceNumber());
            System.out.println(receivedPacket.getPayload());
            this.lastReceivedPacket = receivedPacket;
            return receivedPacket;
        }

        return null;

         */


    }

    @Override
    protected synchronized void setReceived() {
        boolean shouldAddToReceived = receivingPacketIndex(this.inputBuffer.peek()) == 0;
        while (shouldAddToReceived){

            Packet packetReceived = this.dequeueInputBuffer();
            this.logger.log(Level.INFO, () -> "packet: " + packetReceived + " received");

            this.updateConnection(packetReceived);
            if (!this.received.contains(packetReceived)){
                this.ack(packetReceived);
                this.addToReceived(packetReceived);
            }

            if (this.inputBuffer.isEmpty()) return;

            shouldAddToReceived = receivingPacketIndex(this.inputBuffer.peek()) == 0;
        }

        for (Packet packet : this.inputBuffer) {
            if (inReceivingWindow(packet)){
                if (this.received.contains(packet)) continue;
                this.addToReceived(packet);
                this.ack(packet);
            }else{
                boolean removed = this.inputBuffer.remove(packet);
                if (!removed){
                    throw new IllegalStateException("packet that is already acknowledged has fails to be removed from the input buffer");
                }
                this.ack(packet);
            }
        }

    }

    @Override
    public Packet[] packetsToRetransmit() {
        Queue<Packet> retransmit = new PriorityQueue<>(PACKET_COMPARATOR);
        for (WaitingPacket wp : this.waitingPackets) {
            boolean timeoutFinished = wp.timeoutFinished();
            boolean ackNotReceivedOnPacket = !this.receivedAck.contains(wp.getPacket());
            boolean noMatchingWaitingPacketOnAck = this.receivedAck.stream().noneMatch((packet -> packet.getAcknowledgmentNumber() - 1 == wp.getPacket().getSequenceNumber()));
            if (timeoutFinished && ackNotReceivedOnPacket && noMatchingWaitingPacketOnAck){
                boolean added = retransmit.offer(wp.getPacket());
                if (!added) throw new IllegalStateException("a packet was not added to the retransmit queue");
                wp.restart();
            }
        }
        return retransmit.toArray(new Packet[retransmit.size()]);
    }

    @Override
    public boolean isWaitingForACK() {
        return this.waitingPackets.isFull();
    }

    @Override
    protected void addToWaitingPacketWindow(Packet packet){
        WaitingPacket waitingPacket = new WaitingPacket(packet, TIMEOUT_DURATION);
        this.addToWaitingPackets(waitingPacket);
    }

    @Override
    protected synchronized void ackReceived() {
        Packet ack = this.dequeueInputBuffer();

        if (!this.isConnected()){
            logger.log(Level.INFO, "ack received with no connection established");
            return;
        }

        if (this.waitingPackets.isEmpty()){
            logger.log(Level.WARNING, "received ack without any waiting packets. May be from routed (non TCP) packet or possibly uncaught invalid connection ");
            return;
        }

        if (this.receivedAck.contains(ack)){
            logger.log(Level.INFO, "ACK is already received");
            return;
        }

        if (this.waitingPackets.stream().noneMatch(
                (waitingPacket -> waitingPacket.getPacket().getSequenceNumber() == ack.getAcknowledgmentNumber()-1))){
            logger.log(Level.INFO, "ACK received but no packet to be acknowledge was found");
            return;
        }

        this.addToReceivedAck(ack);

        while (receivedAck.peek().getAcknowledgmentNumber() - 1 == waitingPackets.peek().getPacket().getSequenceNumber()){
            this.waitingPackets.poll();
            Packet firstAck = this.receivedAck.poll();
            this.updateConnection(firstAck);

            if (receivedAck.isEmpty()){
                return;
            }

            if(waitingPackets.isEmpty()){
                return;
            }
        }

    }

    @Override
    public int getWindowSize() {
        return WINDOW_SIZE;
    }

    @Override
    protected boolean inSendingWindow(Packet packet){
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

    public boolean hasWaitingPackets() {
        return !this.waitingPackets.isEmpty();
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
