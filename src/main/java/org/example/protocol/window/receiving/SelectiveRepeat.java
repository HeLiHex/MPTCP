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

    public SelectiveRepeat(int windowSize, Connection connection, Comparator<Packet> comparator, Queue<Packet> receivedContainer) {
        super(windowSize, connection, comparator);
        this.received = receivedContainer;
        this.ackThis = new PacketBuilder().withConnection(connection).build();
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

        Packet packet = this.poll();

        if (packet.hasAllFlags(Flag.ACK)) {
            sendingWindow.ackReceived(packet);
            return false;
        }

        if (this.inReceivingWindow(packet)) {
            if (receivingPacketIndex(packet) == 0) {
                this.connection.update(packet);
                this.ackThis = packet;
                this.receive(packet);
                return true;
            }
            this.receive(packet);
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
    public int receivingPacketIndex(Packet packet) {
        int seqNum = packet.getSequenceNumber();
        int ackNum = this.connection.getNextAcknowledgementNumber();
        return seqNum - ackNum;
    }

    @Override
    public boolean inReceivingWindow(Packet packet) {
        int packetIndex = receivingPacketIndex(packet);
        return inWindow(packetIndex);
    }
}
