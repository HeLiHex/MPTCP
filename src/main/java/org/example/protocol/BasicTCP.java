package org.example.protocol;

import org.example.data.Packet;
import org.example.util.BoundedPriorityBlockingQueue;
import org.example.util.WaitingPacket;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger;

    private static final int WINDOW_SIZE = 4;
    private static final int BUFFER_SIZE = 100;
    private static final double NOISE_TOLERANCE = 100.0;
    private static final int TIMEOUT_DURATION = 50;
    private final static Comparator<Packet> PACKET_COMPARATOR = (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber();

    private BlockingQueue<Packet> received;
    private BlockingQueue<WaitingPacket> waitingPackets;
    private BlockingQueue<Packet> receivedAck;

    public BasicTCP(Random randomGenerator) {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                randomGenerator,
                NOISE_TOLERANCE
        );
        this.logger = Logger.getLogger(this.getName());
        this.received = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.waitingPackets = new BoundedPriorityBlockingQueue<>(WINDOW_SIZE);
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
        if (inReceivingWindow(this.inputBuffer.peek())){ //todo - dette skjer mange ganger på rad fordi du bruker peek()
            this.ack(this.inputBuffer.peek());
            return;
        }


        //sending ack on packets that are received but sender does not know its received
        if (receivingPacketIndex(this.inputBuffer.peek()) < getWindowSize()){
            this.ack(this.inputBuffer.poll());
        }

    }

    @Override
    protected Packet[] packetsToRetransmit() {
        ArrayList<Packet> retransmit = new ArrayList<>();
        for (WaitingPacket wp : this.waitingPackets) {
            wp.update();
            //System.out.println("waiting packet: " + wp);
            if (wp.timeoutFinished() && !this.receivedAck.contains(wp.getPacket())){
                retransmit.add(wp.getPacket());
                wp.restart();
            }
        }
        return retransmit.toArray(new Packet[retransmit.size()]);
    }

    @Override
    protected boolean isWaitingForACK() {
        return this.waitingPackets.remainingCapacity() == 0;
    }

    @Override
    protected void addToWaitingPacketWindow(Packet packet){
        WaitingPacket waitingPacket = new WaitingPacket(packet, TIMEOUT_DURATION);
        this.waitingPackets.offer(waitingPacket);
    }

    @Override
    protected void ackReceived() {
        Packet ack = this.dequeueInputBuffer();
        this.receivedAck.add(ack);

        if (this.waitingPackets.isEmpty()){
            logger.log(Level.WARNING, "received ack without any waiting packets. May be from routed (non TCP) packet or passably uncaught invalid connection ");
            return;
        }

        while (receivedAck.peek().getAcknowledgmentNumber() - 1 == waitingPackets.peek().getPacket().getSequenceNumber()){
            this.updateConnection(this.receivedAck.peek());
            this.waitingPackets.poll();
            this.receivedAck.poll();

            if (receivedAck.isEmpty() || waitingPackets.isEmpty()) return;
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
