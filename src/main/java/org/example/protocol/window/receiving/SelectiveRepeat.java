package org.example.protocol.window.receiving;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.protocol.window.sending.SendingWindow;
import org.example.simulator.Statistics;

import java.util.Comparator;
import java.util.List;

public class SelectiveRepeat extends Window implements ReceivingWindow {

    private final List<Packet> received;
    private Packet ackThis;
    private int packetCount;

    public SelectiveRepeat(int windowSize, Comparator<Packet> comparator, List<Packet> receivedContainer) {
        super(windowSize, comparator);
        this.received = receivedContainer;
        this.ackThis = new PacketBuilder().build();
        this.packetCount = 0;
    }

    private void receive(Packet packet) {
        if (this.received.contains(packet)) return;
        boolean added = this.received.add(packet);
        if (!added) throw new IllegalStateException("Packet was not added to the received queue");
        Statistics.packetReceived();
    }

    @Override
    public boolean receive(SendingWindow sendingWindow) {
        if (this.isEmpty()) return false;

        if (this.peek().hasAllFlags(Flag.ACK)) {
            sendingWindow.ackReceived(this.peek());
            this.poll();
            return false;
        }

        Connection connection = sendingWindow.getConnection();

        System.out.println(peek().getIndex());
        System.out.println(peek().getPayload());
        System.out.println(peek().getSequenceNumber());
        System.out.println();

        if (this.inReceivingWindow(this.peek(), connection)) {
            while (receivingPacketIndex(this.peek(), connection) == 0){
                connection.update(this.peek());
                this.ackThis = this.peek();
                while (this.peek().getIndex() <= this.packetCount){
                    connection.update(this.peek());
                    this.ackThis = this.peek();
                    this.receive(this.peek());
                    System.out.println(this.peek() + " received");
                    this.remove();
                    this.packetCount++;
                    if (this.isEmpty()) break;
                }
                if (this.isEmpty()) break;
            }
            return true; // true so that duplicate AKCs are sent
        }
        //false, because packets outside the window has already ben acked
        return false;
    }

    @Override
    public boolean shouldAck() {
        return true;
    }

    @Override
    public Packet ackThis() {
        assert shouldAck();
        return this.ackThis;
    }

    @Override
    public int receivingPacketIndex(Packet packet, Connection connection) {
        int seqNum = packet.getSequenceNumber();
        int ackNum = connection.getNextAcknowledgementNumber();
        return seqNum - ackNum;
    }

    @Override
    public boolean inReceivingWindow(Packet packet, Connection connection) {
        int packetIndex = receivingPacketIndex(packet, connection);
        return inWindow(packetIndex);
    }

    @Override
    public boolean offer(Packet packet) {
        if (this.isFull()) return false;
        if (this.contains(packet)) return false;
        return super.offer(packet);
    }

}
