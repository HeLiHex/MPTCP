package org.example.protocol;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger;

    private static final int WINDOW_SIZE = 10;
    private static final int BUFFER_SIZE = 100;
    private static final double NOISE_TOLERANCE = 100.0;
    private final static Comparator<Packet> PACKET_COMPARATOR = (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber();

    private BlockingQueue<Packet> received;
    private BlockingQueue<Packet> waitingOnAckPackets;
    private BlockingQueue<Packet> receivedAck;

    public BasicTCP(Random randomGenerator) {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                randomGenerator,
                NOISE_TOLERANCE
        );
        this.logger = Logger.getLogger(this.getName());
        this.received = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.waitingOnAckPackets = new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR);
        this.receivedAck = new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR);
    }


    @Override
    public Packet receive() {
        if (received.isEmpty()) return null;
        return received.poll();
    }

    @Override
    protected synchronized void setReceived() {
        if (this.inputBuffer.isEmpty()) return;

        boolean shouldAddToReceived = receivingPacketIndex(this.inputBuffer.peek()) == 0;
        while (shouldAddToReceived){

            Packet received = this.dequeueInputBuffer();
            System.out.println("packet: " + received + " received");

            this.updateConnection(received);
            this.received.offer(received);
            this.ack(received);

            if (this.inputBuffer.isEmpty()) return;

            shouldAddToReceived = receivingPacketIndex(this.inputBuffer.peek()) == 0;
        }
        /*
        if the current first packet in the input buffer is not the first in the window
        then ack the packet if it's inside the window, but not poll because the poll should happen
        in correct order.
         */
        if (inReceivingWindow(this.inputBuffer.peek())){
            this.ack(this.inputBuffer.peek());
        }

    }

    @Override
    protected Packet[] packetsToRetransmit() {
        return new Packet[0];
    }

    @Override
    protected boolean isWaitingForACK() {
        return this.waitingOnAckPackets.remainingCapacity() == 0;
    }

    @Override
    protected void addToWaitingOnAckWindow(Packet packet){
        this.waitingOnAckPackets.offer(packet);
    }

    @Override
    protected void ackReceived() {
        Packet ack = this.dequeueInputBuffer();
        this.receivedAck.add(ack);

        if (this.waitingOnAckPackets.isEmpty()){
            logger.log(Level.WARNING, "received ack without any waiting packets. May be from routed (non TCP) packet or passably uncaught invalid connection ");
            return;
        }

        while (receivedAck.peek().getAcknowledgmentNumber() - 1 == waitingOnAckPackets.peek().getSequenceNumber()){
            this.updateConnection(this.receivedAck.peek());
            this.waitingOnAckPackets.poll();
            this.receivedAck.poll();

            if (receivedAck.isEmpty() || waitingOnAckPackets.isEmpty()) return;
        }

    }

    @Override
    protected int getWindowSize() {
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
}
