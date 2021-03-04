package org.example.protocol.window;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.Comparator;

public abstract class Window extends BoundedPriorityBlockingQueue<Packet> implements IWindow {

    protected static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    protected final Connection connection;
    protected final int windowSize;

    public Window(int windowSize, Connection connection) {
        super(100000, PACKET_COMPARATOR);
        this.connection = connection;
        this.windowSize = windowSize;
    }

    @Override
    public boolean inSendingWindow(Packet packet){
        int packetIndex = sendingPacketIndex(packet);
        return inWindow(packetIndex);
    }

    @Override
    public boolean inReceivingWindow(Packet packet){
        int packetIndex = receivingPacketIndex(packet);
        return inWindow(packetIndex);
    }

    private boolean inWindow(int packetIndex){
        int windowSize = this.windowSize;
        return packetIndex < windowSize && packetIndex >= 0;
    }

    @Override
    public int sendingPacketIndex(Packet packet){
        Connection conn = this.connection;
        int packetSeqNum = packet.getSequenceNumber();
        int connSeqNum = conn.getNextSequenceNumber();
        return packetSeqNum - connSeqNum;
    }


    @Override
    public int receivingPacketIndex(Packet packet){
        Connection conn = this.connection;
        int seqNum = packet.getSequenceNumber();
        int ackNum = conn.getNextAcknowledgementNumber();
        return seqNum - ackNum;
    }

}
