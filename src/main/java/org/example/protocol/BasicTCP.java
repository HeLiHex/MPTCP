package org.example.protocol;

import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.simulator.Statistics;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger;

    private Packet lastAcknowledged;

    private static final int WINDOW_SIZE = 7;
    private static final int BUFFER_SIZE = 10000;
    private static final double NOISE_TOLERANCE = 100.0;
    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);

    private final Queue<Packet> received;

    public BasicTCP() {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                NOISE_TOLERANCE
        );
        this.logger = Logger.getLogger(this.getClass().getSimpleName());

        this.received = new PriorityQueue<>(PACKET_COMPARATOR);
    }

    private void addToReceived(Packet packet) {
        if (this.received.contains(packet)) return;
        boolean added = this.received.offer(packet);
        if (!added) throw new IllegalStateException("Packet was not added to the received queue");
        Statistics.packetReceived();
    }

    @Override
    public Packet receive() {
        return ((ReceivingWindow)this.inputBuffer).getReceivedPackets().poll();
    }

    private void ack(Packet packet) {
        this.lastAcknowledged = packet;
        Packet ack = new PacketBuilder().ackBuild(packet);
        this.route(ack);
    }


    private void dupAck() {
        if (this.lastAcknowledged == null) {
            this.lastAcknowledged = new PacketBuilder()
                    .withConnection(this.getConnection())
                    .build();
        }
        this.ack(this.lastAcknowledged);
        return;
    }

    @Override
    protected void setReceived(Packet packet) {
        ReceivingWindow receivingWindow = (ReceivingWindow) this.inputBuffer;
        receivingWindow.receive();
        this.ack(receivingWindow.ackThis());

        /*
        if (packet == null) throw new IllegalStateException("null packet received");
        if (this.inReceivingWindow(packet)) {
            if (receivingPacketIndex(packet) == 0) {
                this.updateConnection(packet);
                this.ack(packet);
                this.addToReceived(packet);
                return;
            }
            this.addToReceived(packet);
        }
        this.dupAck();
        //return this.dupAck(); //if a dupACK was sent or not

         */
    }


    @Override
    public int getWindowSize() {
        return WINDOW_SIZE;
    }

    @Override
    protected boolean inReceivingWindow(Packet packet) {
        int packetIndex = receivingPacketIndex(packet);
        return inWindow(packetIndex);
    }

    private boolean inWindow(int packetIndex) {
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
