package org.example.protocol;

import org.example.data.*;
import org.example.network.interfaces.Endpoint;
import org.example.network.RoutableEndpoint;
import org.example.simulator.Statistics;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTCP extends RoutableEndpoint implements TCP {

    private final Logger logger = Logger.getLogger(AbstractTCP.class.getName());
    private Connection connection;
    private int initialSequenceNumber;


    protected AbstractTCP(BlockingQueue<Packet> inputBuffer,
                       BlockingQueue<Packet> outputBuffer,
                       Random randomGenerator,
                       double noiseTolerance) {
        super(inputBuffer,
                outputBuffer,
                randomGenerator,
                noiseTolerance);
    }


    @Override
    public void connect(Endpoint host) {
        this.updateRoutingTable();
        this.initialSequenceNumber = this.random(100);
        Packet syn = new PacketBuilder()
                .withDestination(host)
                .withOrigin(this)
                .withFlags(Flag.SYN)
                .withSequenceNumber(this.initialSequenceNumber)
                .build();
        this.route(syn);
    }

    public void continueConnect(){
        Packet synAck = this.dequeueInputBuffer();
        Endpoint host = synAck.getOrigin();
        if (synAck.hasAllFlags(Flag.SYN, Flag.ACK)){
            if (synAck.getAcknowledgmentNumber() != this.initialSequenceNumber + 1){
                this.logger.log(Level.INFO, "Wrong ack number");
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

            this.setConnection(new Connection(this, host, finalSeqNum, ackNum));
            this.logger.log(Level.INFO, () -> "connection established with host: " + this.getConnection());
        }

    }

    @Override
    public void connect(Packet syn){
        Endpoint node = syn.getOrigin();
        int seqNum = random(100);
        int ackNum = syn.getSequenceNumber() + 1;
        Packet synAck = new PacketBuilder()
                .withDestination(node)
                .withOrigin(this)
                .withFlags(Flag.SYN, Flag.ACK)
                .withSequenceNumber(seqNum)
                .withAcknowledgmentNumber(ackNum)
                .build();

        this.setConnection(new Connection(this, node, seqNum, ackNum));
        this.logger.log(Level.INFO, () -> "connection established with: " + this.getConnection());
        this.addToWaitingPacketWindow(synAck);

        this.route(synAck);
    }

    @Override
    public void send(Packet packet) {
        int nextPacketSeqNum = this.getConnection().getNextSequenceNumber() + this.outputBuffer.size();
        packet.setSequenceNumber(nextPacketSeqNum);

        boolean wasAdded = this.enqueueOutputBuffer(packet);
        if (!wasAdded) {
            logger.log(Level.WARNING, "Packet was not added to the output queue");
        }
    }

    @Override
    public void send(Payload payload) {
        Packet packet = new PacketBuilder()
                .withConnection(this.getConnection())
                .withPayload(payload)
                .build();
        send(packet);
    }

    protected abstract void setReceived();

    @Override
    public void close() {
        Packet fin = new PacketBuilder()
                .withConnection(this.getConnection())
                .withFlags(Flag.FIN)
                .build();
        send(fin);
    }

    protected abstract int getWindowSize();

    public abstract boolean isWaitingForACK();

    protected abstract void addToWaitingPacketWindow(Packet packet);

    protected abstract boolean inReceivingWindow(Packet packet);

    protected abstract boolean inSendingWindow(Packet packet);

    protected abstract void ackReceived();

    @Override
    public boolean isConnected() {
        return this.connection != null;
    }

    @Override
    public Endpoint getConnectedEndpoint() {
        return getConnection().getConnectedNode();
    }

    public synchronized Connection getConnection() {
        if (this.connection == null){
            throw new IllegalStateException("connection is null");
        }
        return this.connection;
    }

    protected void updateConnection(Packet packet){
        this.connection.update(packet);
    }

    protected synchronized void setConnection(Connection connection) {
        this.connection = connection;
    }

    protected void closeConnection() {
        if (this.connection == null){
            logger.log(Level.WARNING, "There is noe connection to be closed");
            return;
        }
        logger.log(Level.INFO, () -> "Connection to " + this.connection.getConnectedNode() + " is closed");
        this.connection = null;
    }

    protected int sendingPacketIndex(Packet packet){
        Connection conn = this.getConnection();
        int packetSeqNum = packet.getSequenceNumber();
        int connSeqNum = conn.getNextSequenceNumber();
        return packetSeqNum - connSeqNum;
    }

    protected int receivingPacketIndex(Packet packet){
        Connection conn = this.getConnection();
        int seqNum = packet.getSequenceNumber();
        int ackNum = conn.getNextAcknowledgementNumber();
        return seqNum - ackNum;
    }

    private boolean packetIsFromValidConnection(Packet packet) {
        if (packet.hasAllFlags(Flag.SYN)) return true;
        Connection conn = this.getConnection();
        if (conn == null) return false;
        if (packet.hasAllFlags(Flag.ACK)) return true;
        return packet.getOrigin().equals(conn.getConnectedNode())
                && packet.getDestination().equals(conn.getConnectionSource()
        );
    }

    @Override
    public synchronized boolean enqueueInputBuffer(Packet packet) {
        boolean shouldEnqueue = packet.hasAllFlags(Flag.ACK) || packet.hasAllFlags(Flag.SYN) || packetIsFromValidConnection(packet);
        if (shouldEnqueue){
            return super.enqueueInputBuffer(packet);
        }
        this.logger.log(Level.INFO, "packet was not added due to non valid connection");
        return false;
    }

    protected void ack(Packet packet){
        Packet ack = new PacketBuilder().ackBuild(packet);
        this.route(ack);
    }


    public void handleIncoming(){
        if (this.inputBufferIsEmpty()){
            return;
        }

        Packet packet = this.inputBuffer.peek();

        if (packet.hasAllFlags(Flag.SYN, Flag.ACK)){
            this.continueConnect();
        }

        if (packet.hasAllFlags(Flag.ACK)){
            this.ackReceived();
            return;
        }

        if (packet.hasAllFlags(Flag.SYN)){
            this.connect(packet);
            this.dequeueInputBuffer();
            return;
        }

        this.setReceived();
    }

    public void retransmit(){
        Packet[] packets = packetsToRetransmit();
        for (Packet packet : packets) {
            logger.log(Level.INFO, () -> "retransmitting packet " + packet + "-----------------------------------");
            Statistics.packetRetransmit();
            this.route(packet);
        }
    }

    public abstract Packet[] packetsToRetransmit();

    public Packet trySend(){
        if (this.outputBufferIsEmpty()){
            return null;
        }
        if (this.isWaitingForACK()){
            return null;
        }

        if (!this.inSendingWindow(this.outputBuffer.peek())){
            logger.log(Level.WARNING, "Trying to send Packet out of order. This should not happen");
            return null;
        }

        Packet packet = this.dequeueOutputBuffer();
        this.addToWaitingPacketWindow(packet);
        this.route(packet);
        Statistics.packetSent();
        logger.log(Level.INFO, () -> "packet: " + packet + " sent");
        return packet;
    }

    @Override
    public void run() {
        this.handleIncoming();
        this.retransmit();
        this.trySend();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
