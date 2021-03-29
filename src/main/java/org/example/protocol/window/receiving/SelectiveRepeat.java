package org.example.protocol.window.receiving;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.protocol.window.sending.SendingWindow;
import org.example.simulator.Statistics;

import java.util.Comparator;
import java.util.Queue;

public class SelectiveRepeat extends Window implements ReceivingWindow {

    private final Queue<Packet> received;
    private Packet ackThis;

    public SelectiveRepeat(int windowSize, Comparator<Packet> comparator, Queue<Packet> receivedContainer) {
        super(windowSize, comparator);
        this.received = receivedContainer;
        this.ackThis = new PacketBuilder().build();
    }

    private void receive(Packet packet) {
        if (this.received.contains(packet)) return;
        boolean added = this.received.offer(packet);
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

        if (this.inReceivingWindow(this.peek(), connection)) {
            while (receivingPacketIndex(this.peek(), connection) == 0){
                connection.update(this.peek());
                this.ackThis = this.peek();
                this.receive(this.peek());
                System.out.println(this.peek() + " received");
                System.out.println(this);
                System.out.println(this.size());
                this.remove();
                if (this.isEmpty()) return true;
            }
            System.out.println(this);
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
        System.out.println("packet offered: " + packet);
        if (this.remainingCapacity() == 0){
            System.out.println("receiver window full");
            System.out.println(this.size());
            System.out.println(this);
        }
        if (this.contains(packet)){
            System.out.println("contains: " + packet);
            return false;
        }
        return super.offer(packet);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Packet packet : this) {
            stringBuilder.append("[");
            stringBuilder.append(packet);
            stringBuilder.append("]");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
