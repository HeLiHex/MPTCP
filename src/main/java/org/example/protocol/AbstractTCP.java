package org.example.protocol;

import org.example.data.*;
import org.example.network.interfaces.Endpoint;
import org.example.network.RoutableEndpoint;


import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTCP extends RoutableEndpoint implements TCP {

    private final Logger logger = Logger.getLogger(AbstractTCP.class.getName());


    public AbstractTCP(BlockingQueue<Packet> inputBuffer, BlockingQueue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
        super(inputBuffer, outputBuffer, randomGenerator, noiseTolerance);
    }


    @Override
    public void connect(Endpoint host) {
        this.updateRoutingTable();
        int initSeqNum = random(100);
        Packet syn = new Packet.PacketBuilder()
                .withDestination(host)
                .withOrigin(this)
                .withFlags(Flag.SYN)
                .withSequenceNumber(initSeqNum)
                .build();
        this.route(syn);

        while (this.inputBufferIsEmpty()){
            this.sleep();
        }

        Packet synAck = this.dequeueInputBuffer();
        if (synAck.hasFlag(Flag.SYN, Flag.ACK)){
            if (synAck.getAcknowledgmentNumber() != initSeqNum + 1){
                System.err.println("wrong ack number");
                return;
            }
            System.out.println(synAck);
            int finalSeqNum = synAck.getAcknowledgmentNumber();
            int ackNum = synAck.getSequenceNumber() + 1;
            Packet ack = new Packet.PacketBuilder()
                    .withDestination(host)
                    .withOrigin(this)
                    .withFlags(Flag.ACK)
                    .withSequenceNumber(finalSeqNum)
                    .withAcknowledgmentNumber(ackNum)
                    .build();
            this.route(ack);

            this.setConnection(new Connection(this, host, finalSeqNum, ackNum, getWindowSize()));
            this.logger.log(Level.INFO, "connection established with host: " + this.getConnection());
            this.start();
        }
    }

    @Override
    public void connect(Packet syn){
        Endpoint node = syn.getOrigin();
        int seqNum = random(100);
        int ackNum = syn.getSequenceNumber() + 1;
        Packet synAck = new Packet.PacketBuilder()
                .withDestination(node)
                .withOrigin(this)
                .withFlags(Flag.SYN, Flag.ACK)
                .withSequenceNumber(seqNum)
                .withAcknowledgmentNumber(ackNum)
                .build();
        this.route(synAck);

        this.setConnection(new Connection(this, node, seqNum, ackNum, getWindowSize()));
        this.logger.log(Level.INFO, "connection established with: " + this.getConnection());
    }

    @Override
    public void send(Packet packet) {
        boolean wasAdded = this.enqueueOutputBuffer(packet);
        if (!wasAdded) {
            logger.log(Level.WARNING, "Packet was not added to the output queue");
            return;
        }
        System.out.println("packet: " + packet + " sent");
    }

    @Override
    public void send(Payload payload) {
        Packet packet = new Packet.PacketBuilder()
                .withSequenceNumber(this.getConnection().getNextSequenceNumber())
                .withConnection(this.getConnection())
                .withPayload(payload)
                .build();
        send(packet);
    }

    @Override
    public abstract Packet receive();

    protected abstract void setReceived();

    @Override
    public void close() {
        Packet fin = new Packet.PacketBuilder()
                .withConnection(this.getConnection())
                .withFlags(Flag.FIN)
                .build();
        send(fin);
    }

    protected abstract int getWindowSize();

    protected abstract boolean isWaitingForACK();

    protected abstract boolean inWindow(Packet packet);

    protected abstract Connection getConnection();

    protected abstract void updateConnection(Packet packet);

    protected abstract void setConnection(Connection connection);

    protected abstract void closeConnection();

    protected int packetIndex(Packet packet){
        Connection conn = this.getConnection();
        int seqNum = packet.getSequenceNumber();
        int ackNum = conn.getNextAcknowledgementNumber();

        return seqNum - ackNum;
    }

    private boolean packetIsFromValidConnection(Packet packet) {
        if (packet.hasFlag(Flag.SYN)) return true;
        Connection conn = this.getConnection();
        if (conn == null) return false;
        return this.inWindow(packet)
                && packet.getOrigin().equals(conn.getConnectedNode())
                && packet.getDestination().equals(conn.getConnectionSource()
        );
    }

    @Override
    public synchronized boolean enqueueInputBuffer(Packet packet) {
        if (!packetIsFromValidConnection(packet)) return false;
        return super.enqueueInputBuffer(packet);
    }

    private void ack(Packet packet, Flag... flags){
        //add ack to flags
        Flag[] flagsWithAck = new Flag[flags.length + 1];
        for (int i = 0; i < flags.length; i++) {
            flagsWithAck[i] = flags[i];
        }
        flagsWithAck[flagsWithAck.length-1] = Flag.ACK;

        this.route(new Packet.PacketBuilder().ackBuild(packet));
    }


    private void handleIncoming(){
        if (this.inputBufferIsEmpty()){
            sleep();
            return;
        }

        Packet packet = this.inputBuffer.peek();

        if (packet.hasFlag(Flag.ACK)){
            this.updateConnection(packet);
            this.dequeueInputBuffer();
            return;
        }

        if (packet.hasFlag(Flag.SYN)){
            this.connect(packet);
            this.dequeueInputBuffer();
            return;
        }

        this.ack(packet);
        //this.updateConnection(packet);
        this.setReceived(); //this.dequeueInputBuffer();
        return;

    }


    private void trySend(){
        if (this.outputBufferIsEmpty()){
            //logger.log(Level.INFO, "outputbuffer is empty");
            this.sleep();
            return;
        }
        if (isWaitingForACK()){
            logger.log(Level.INFO, "waiting for ack");
            this.sleep();
            return;
        }

        Packet packet = this.dequeueOutputBuffer();
        this.route(packet);
        if (packet.hasFlag(Flag.ACK)) return;
    }

    private void sleep(){
        try {
            sleep(10);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread Interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (true){
            this.handleIncoming();
            this.trySend();
        }
    }


}
