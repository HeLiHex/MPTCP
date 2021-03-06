package org.example.protocol.window;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.Comparator;

public abstract class Window extends BoundedPriorityBlockingQueue<Packet> implements IWindow {

    protected final Connection connection;

    public Window(int windowSize, Connection connection, Comparator comparator) {
        super(windowSize, comparator);
        this.connection = connection;
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
        return packetIndex < this.getWindowSize() && packetIndex >= 0;
    }

    @Override
    public int sendingPacketIndex(Packet packet){
        int packetSeqNum = packet.getSequenceNumber();
        int connSeqNum = this.connection.getNextSequenceNumber();
        return packetSeqNum - connSeqNum;
    }


    @Override
    public int receivingPacketIndex(Packet packet){
        int seqNum = packet.getSequenceNumber();
        int ackNum = this.connection.getNextAcknowledgementNumber();
        return seqNum - ackNum;
    }

    @Override
    public int getWindowSize() {
        return this.bound();
    }
}
