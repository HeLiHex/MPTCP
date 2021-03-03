package org.example.protocol.window.receiving;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.protocol.window.sending.SendingWindow;

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

    @Override
    public boolean receive(SendingWindow sendingWindow) {
        Packet packet = this.poll();

        if (packet.hasAllFlags(Flag.ACK)){
            sendingWindow.ackReceived(packet);
            return false;
        }

        if (packet == null) throw new IllegalStateException("null packet received");
        if (this.inWindow(packet)) {
            if (packetIndex(packet) == 0) {
                this.connection.update(packet);
                this.ackThis = packet;
                this.received.offer(packet);
                return true;
            }
            this.received.offer(packet);
        }
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
