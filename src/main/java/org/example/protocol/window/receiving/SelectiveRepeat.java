package org.example.protocol.window.receiving;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.protocol.window.sending.SendingWindow;
import org.example.simulator.Statistics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelectiveRepeat extends Window implements ReceivingWindow {

    private final List<Packet> received;
    private int packetCount;
    private Map<Endpoint, Packet> ackThisMap;
    private boolean shouldAck;

    public SelectiveRepeat(int windowSize, Comparator<Packet> comparator, List<Packet> receivedContainer) {
        super(windowSize, comparator);
        this.received = receivedContainer;
        this.packetCount = 0;
        this.ackThisMap = new HashMap<>();
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

        Connection connection = sendingWindow.getConnection();

        if (this.peek().hasAllFlags(Flag.ACK)) {
            if (this.peek().getOrigin().equals(connection.getConnectedNode())){
                sendingWindow.ackReceived(this.peek());
                this.poll();
            }
            return false;
        }



        Packet packetToUpdateWith = this.ackThisMap.get(connection.getConnectedNode());
        if (packetToUpdateWith != null) connection.update(packetToUpdateWith);




        /*
        System.out.println(peek().getIndex());
        System.out.println(peek().getPayload());
        System.out.println(peek().getSequenceNumber());
        System.out.println();

         */

        System.out.println(size());
        System.out.println();
        System.out.println(this);
        System.out.println();

        if (this.inReceivingWindow(this.peek(), connection)) {
            while (receivingPacketIndex(this.peek(), connection) == 0 && this.peek().getIndex() == this.packetCount){
                connection.update(this.peek());
                this.ackThisMap.put(this.peek().getOrigin(), this.peek());
                this.receive(this.peek());
                System.out.println(this.peek() + " received");
                this.remove();
                this.packetCount++;
                if (this.isEmpty()) return true;
            }

            /*while (this.peek().getIndex() == this.packetCount){
                if (receivingPacketIndex(this.peek(), connection) == 0) connection.update(this.peek());
                this.ackThisMap.put(this.peek().getOrigin(), this.peek());
                this.receive(this.peek());
                System.out.println(this.peek() + " received");
                this.remove();
                this.packetCount++;
                if (this.isEmpty()) return true;
            }*/
            return true; // true so that duplicate AKCs are sent
        }
        //false, because packets outside the window has already ben acked
        assert !this.isFull();
        return false;
    }

    @Override
    public boolean shouldAck() {
        return true;
    }

    @Override
    public Packet ackThis(Endpoint endpointToReceiveAck) {
        assert shouldAck();
        return this.ackThisMap.getOrDefault(endpointToReceiveAck, new PacketBuilder().build());
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
        if (this.isFull()){
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, () -> packet + " lost due to capacity");
            return false;
        }
        if (this.contains(packet)){
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, () -> packet + " is already in queue");
            //System.out.println(this.received);
            return false;
        }
        return super.offer(packet);
    }

}
