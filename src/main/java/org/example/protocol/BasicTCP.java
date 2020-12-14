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

    private final Logger logger = Logger.getLogger(BasicTCP.class.getName());

    private static final int WINDOW_SIZE = 50;
    private static final int BUFFER_SIZE = 100;
    private static final double NOISE_TOLERANCE = 100.0;
    private volatile Connection connection;
    private BlockingQueue<Packet> received;
    private final static Comparator<Packet> PACKET_COMPARATOR = (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber();
    private BoundedPriorityBlockingQueue<Packet> waitingOnAckPackets;

    public BasicTCP(Random randomGenerator) {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                randomGenerator,
                NOISE_TOLERANCE
        );
        this.received = new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.waitingOnAckPackets = new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR);
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

            if (this.inputBuffer.isEmpty()) return;

            shouldAddToReceived = receivingPacketIndex(this.inputBuffer.peek()) == 0;
        }
    }

    @Override
    protected synchronized Connection getConnection() {
        while (this.connection == null){
            logger.log(Level.WARNING, "no connection established!");
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.connection;
    }

    @Override
    protected void updateConnection(Packet packet){
        this.connection.update(packet);
    }

    @Override
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected void closeConnection() {
        if (this.connection == null){
            logger.log(Level.WARNING, "There is noe connection to be closed");
            return;
        }
        logger.log(Level.INFO, () -> "Connection to " + this.connection.getConnectedNode() + " is closed");
        this.connection = null;
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
        Packet waitingPacket = this.waitingOnAckPackets.peek();
        if (waitingPacket == null) return;

        boolean shouldRelease = ack.getAcknowledgmentNumber() - 1 == waitingPacket.getSequenceNumber();
        if (shouldRelease){
            this.waitingOnAckPackets.poll();
        }
    }

    @Override
    protected int getWindowSize() {
        return WINDOW_SIZE;
    }

    @Override
    protected boolean inSendingWindow(Packet packet){
        int packetIndex = sendingPacketIndex(packet);
        int windowSize = this.getWindowSize();
        return packetIndex < windowSize && packetIndex >= 0;
    }

    @Override
    protected boolean inReceivingWindow(Packet packet){
        if (packet.hasFlag(Flag.ACK)) return true; // todo - ack is not in sending window. this is a hack. fix
        int packetIndex = receivingPacketIndex(packet);
        int windowSize = this.getWindowSize();
        return packetIndex < windowSize && packetIndex >= 0;
    }
}
