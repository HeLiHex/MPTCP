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


    private static final int WINDOW_SIZE = 7;
    private static final int BUFFER_SIZE = 10000;
    private static final double NOISE_TOLERANCE = 100.0;
    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);

    private final BlockingQueue<Packet> received;
    private final BoundedPriorityBlockingQueue<Packet> waitingPackets;

    public BasicTCP() {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                NOISE_TOLERANCE
        );
        this.logger = Logger.getLogger(this.getClass().getSimpleName());

        this.received = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.waitingPackets = new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR);
    }

    private void addToReceived(Packet packet){
        if (this.received.contains(packet)) return;
        boolean added = this.received.offer(packet);
        if (!added) throw new IllegalStateException("Packet was not added to the received queue");
        Statistics.packetReceived();
    }

    @Override
    public Packet receive() {
        return this.received.poll();
    }

    private void ack(Packet packet){
        this.lastAcknowledged = packet;
        Packet ack = new PacketBuilder().ackBuild(packet);
        this.route(ack);
    }

    private boolean dupAck(){
        if (this.lastAcknowledged == null){
            return false;
        }
        this.ack(this.lastAcknowledged);
        return true;
    }

    @Override
    protected boolean setReceived(Packet packet) {
        if (packet == null) throw new IllegalStateException("null packet received");
        if (this.inReceivingWindow(packet)){
            if (receivingPacketIndex(packet) == 0){
                this.updateConnection(packet);
                this.ack(packet);
                this.addToReceived(packet);
                return true;
            }
            this.addToReceived(packet);
        }
        boolean dupAckSent = this.dupAck();
        return dupAckSent;
    }

    public boolean packetIsWaiting(Packet packetToMatch){
        return waitingPackets.contains(packetToMatch);
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
    protected void ackReceived(Packet ack) {
        if (!this.isConnected()){
            logger.log(Level.INFO, "ack received with no connection established");
            return;
        }
        int ackIndex = sendingPacketIndex(ack);
        for (int i = 0; i <= ackIndex; i++) {
            waitingPackets.poll();
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
