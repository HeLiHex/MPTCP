package org.example.protocol;

import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger;

    private Packet lastAcknowledged;

    private static final int BUFFER_SIZE = 10000;
    private static final double NOISE_TOLERANCE = 100.0;
    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);

    public BasicTCP() {
        super(new BoundedPriorityBlockingQueue<>(WINDOW_SIZE, PACKET_COMPARATOR),
                new PriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                NOISE_TOLERANCE
        );
        this.logger = Logger.getLogger(this.getClass().getSimpleName());
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

    @Override
    protected void setReceived(Packet packet) {
        ReceivingWindow receivingWindow = (ReceivingWindow) this.inputBuffer;
        receivingWindow.receive();
        this.ack(receivingWindow.ackThis());
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
