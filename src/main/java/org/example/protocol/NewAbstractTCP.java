package org.example.protocol;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.util.Util;

import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class NewAbstractTCP implements TCP, Endpoint{

    private Connection connection;
    private int initialSequenceNumber;
    private long rtt;


    public boolean unconnectedInputHandler(Packet packet) {
        if (packet.hasAllFlags(Flag.SYN, Flag.ACK)) {
            this.continueConnect(packet);
            return true;
        }

        if (packet.hasAllFlags(Flag.SYN)) {
            this.connect(packet);
            return true;
        }

        if (packet.hasAllFlags(Flag.ACK)) {
            System.out.println("creating connection");
            Logger.getLogger(this.getClass().getSimpleName())
                    .log(Level.INFO, () -> "connection established with: " + this.getConnection());
            return false;
        }
        return false;
    }

    public abstract void route(Packet packet);

    public Connection getConnection(){
        return this.connection;
    }

    @Override
    public void connect(Endpoint host) {
        this.connection = null;
        this.initialSequenceNumber = Util.getNextRandomInt(100);
        Packet syn = new PacketBuilder()
                .withDestination(host)
                .withOrigin(this)
                .withFlags(Flag.SYN)
                .withSequenceNumber(this.initialSequenceNumber)
                .build();
        this.route(syn);
    }

    public void continueConnect(Packet synAck) {
        Endpoint host = synAck.getOrigin();
        if (synAck.hasAllFlags(Flag.SYN, Flag.ACK)) {
            if (synAck.getAcknowledgmentNumber() != this.initialSequenceNumber + 1) {
                return;
            }
            int finalSeqNum = synAck.getAcknowledgmentNumber();
            int ackNum = synAck.getSequenceNumber() + 1;
            Packet ack = new PacketBuilder()
                    .withDestination(host)
                    .withOrigin(this)
                    .withFlags(Flag.ACK)
                    .withSequenceNumber(finalSeqNum)
                    .withAcknowledgmentNumber(ackNum)
                    .build();
            this.route(ack);

            this.rtt = Util.seeTime();
            this.connection = new Connection(this, host, finalSeqNum, ackNum);
            //this.logger.log(Level.INFO, () -> "connection established with host: " + this.connection);
        }

    }

    @Override
    public void connect(Packet syn) {
        Endpoint node = syn.getOrigin();
        int seqNum = Util.getNextRandomInt(100);
        int ackNum = syn.getSequenceNumber() + 1;
        Packet synAck = new PacketBuilder()
                .withDestination(node)
                .withOrigin(this)
                .withFlags(Flag.SYN, Flag.ACK)
                .withSequenceNumber(seqNum)
                .withAcknowledgmentNumber(ackNum)
                .build();

        this.connection = new Connection(this, node, seqNum, ackNum);
        this.rtt = Util.seeTime() * 2;
        this.route(synAck);
    }

    @Override
    public void send(Packet packet) {
        if (!this.isConnected()) return;
        int nextPacketSeqNum = this.connection.getNextSequenceNumber() + this.outputBufferSize();
        packet.setSequenceNumber(nextPacketSeqNum);

        boolean wasAdded = this.enqueueOutputBuffer(packet);
        if (!wasAdded) {
            Logger.getLogger(this.getClass().getSimpleName())
                    .log(Level.WARNING, "Packet was not added to the output queue");
        }
    }

    @Override
    public void send(Payload payload) {
        if (!this.isConnected()) return;
        Packet packet = new PacketBuilder()
                .withConnection(this.connection)
                .withPayload(payload)
                .build();
        this.send(packet);
    }

    @Override
    public Packet receive() {
        return null;
    }

    @Override
    public void close() {
        Packet fin = new PacketBuilder()
                .withConnection(this.connection)
                .withFlags(Flag.FIN)
                .build();
        this.send(fin);
    }

    @Override
    public boolean isConnected() {
        return this.connection != null;
    }

    @Override
    public abstract Channel getChannel();

    @Override
    public Endpoint getConnectedEndpoint() {
        return this.connection.getConnectedNode();
    }

    @Override
    public long getRTT() {
        return this.rtt;
    }

    @Override
    public long getRTO() {
        return 4 * this.rtt;
    }


    protected int sendingPacketIndex(Packet packet) {
        Connection conn = this.connection;
        int packetSeqNum = packet.getSequenceNumber();
        int connSeqNum = conn.getNextSequenceNumber();
        return packetSeqNum - connSeqNum;
    }

    protected int receivingPacketIndex(Packet packet) {
        Connection conn = this.connection;
        int seqNum = packet.getSequenceNumber();
        int ackNum = conn.getNextAcknowledgementNumber();
        return seqNum - ackNum;
    }

    protected boolean packetIsFromValidConnection(Packet packet) {
        if (packet.hasAllFlags(Flag.SYN)) return true;
        Connection conn = this.connection;
        if (conn == null) return true;
        if (packet.hasAllFlags(Flag.ACK)) return true;
        return packet.getOrigin().equals(conn.getConnectedNode())
                && packet.getDestination().equals(conn.getConnectionSource()
        );
    }



}
