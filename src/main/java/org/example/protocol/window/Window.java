package org.example.protocol.window;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.Comparator;

public abstract class Window extends BoundedPriorityBlockingQueue<Packet> {

    protected static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    protected final Connection connection;
    protected final int windowSize;

    public Window(int windowSize, Connection connection) {
        super(100000, PACKET_COMPARATOR);
        this.connection = connection;
        this.windowSize = windowSize;
    }

    protected boolean InWindow(Packet packet){
        int packetIndex = packetIndex(packet);
        return inWindow(packetIndex);
    }

    private boolean inWindow(int packetIndex) {
        int windowSize = this.windowSize;
        return packetIndex < windowSize && packetIndex >= 0;
    }

    protected int packetIndex(Packet packet) {
        Connection conn = this.connection;
        int packetSeqNum = packet.getSequenceNumber();
        int connSeqNum = conn.getNextSequenceNumber();
        return packetSeqNum - connSeqNum;
    }

}
