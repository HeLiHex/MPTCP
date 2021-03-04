package org.example.protocol.window.receiving;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.protocol.window.sending.SendingWindow;
import org.example.simulator.Statistics;

import java.util.PriorityQueue;
import java.util.Queue;

public class SelectiveRepeat extends Window implements ReceivingWindow {

    private Queue<Packet> received;
    private Packet ackThis;

    public SelectiveRepeat(int windowSize, Connection connection) {
        super(windowSize, connection);
        this.received = new PriorityQueue<>(PACKET_COMPARATOR);
        this.ackThis = new PacketBuilder().withConnection(connection).build();
    }

    private void receive(Packet packet){
        if (this.received.contains(packet)) return;
        boolean added = this.received.offer(packet);
        if (!added) throw new IllegalStateException("Packet was not added to the received queue");
        Statistics.packetReceived();
    }

    @Override
    public boolean receive() {
        Packet packet = this.poll();
        if (packet == null) throw new IllegalStateException("null packet received");
        if (this.inReceivingWindow(packet)) {
            if (receivingPacketIndex(packet) == 0) {
                this.connection.update(packet);
                this.ackThis = packet;
                this.receive(packet);
                return true;
            }
            this.receive(packet);
        }
        return true;
    }

    @Override
    public boolean shouldAck() {
        return true;
    }

    @Override
    public Packet ackThis() {
        return this.ackThis;
    }

    @Override
    public Queue<Packet> getReceivedPackets() {
        return this.received;
    }
}
