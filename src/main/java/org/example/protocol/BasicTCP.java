package org.example.protocol;

import org.example.data.BufferQueue;
import org.example.data.Flag;
import org.example.data.Packet;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger = Logger.getLogger(BasicTCP.class.getName());

    private static final int BUFFER_SIZE = 4;
    private static final double NOISE_TOLERANCE = 100.0;
    private boolean waitingForACK;
    private volatile Connection connection;
    private BlockingQueue<Packet> received;
    private BlockingQueue<Packet> acknowledged;
    private final static Comparator<Packet> PACKET_COMPARATOR = (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber();

    public BasicTCP(Random randomGenerator) {
        super(new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, (PACKET_COMPARATOR)),
                new ArrayBlockingQueue<>(100),
                randomGenerator,
                NOISE_TOLERANCE
        );
        this.received = new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.acknowledged = new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR);
        this.waitingForACK = false;
    }



    @Override
    public Packet receive() {
        if (received.isEmpty()) return null;
        return received.poll();
    }

    @Override
    protected int getWindowSize() {
        return BUFFER_SIZE;
    }

    @Override
    protected void addToAcked(Packet ackedPacket) {
        this.acknowledged.offer(ackedPacket);
    }

    @Override
    protected synchronized void setReceived() {
        if (this.acknowledged.isEmpty() || this.inputBuffer.isEmpty()) return;

        boolean shouldAddToReceived = packetIndex(this.acknowledged.peek()) == 0;
        while (shouldAddToReceived){

            Packet received = this.dequeueInputBuffer();
            System.out.println("packet: " + received + " received");

            this.updateConnection(received);
            this.received.offer(received);
            this.acknowledged.remove();
            this.updateConnection(received);

            if (this.inputBuffer.isEmpty() || this.acknowledged.isEmpty()) return;

            shouldAddToReceived = packetIndex(this.acknowledged.peek()) == 0;
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
        return false;
    }

    @Override
    protected boolean inWindow(Packet packet){
        if (packet.hasFlag(Flag.ACK)) return true;
        int packetIndex = packetIndex(packet);
        int windowSize = this.getWindowSize();
        return packetIndex < windowSize && packetIndex >= 0;
    }
}
