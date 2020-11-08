package org.example.protocol;

import org.example.data.Flag;
import org.example.data.Payload;
import org.example.network.interfaces.Endpoint;
import org.example.data.BufferQueue;
import org.example.data.Packet;
import org.example.network.RoutableEndpoint;


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTCP extends RoutableEndpoint implements TCP {

    private final Logger logger = Logger.getLogger(AbstractTCP.class.getName());

    private Connection connection;
    private Packet receivedPacket;


    public AbstractTCP(BufferQueue<Packet> inputBuffer, BufferQueue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
        super(inputBuffer, outputBuffer, randomGenerator, noiseTolerance);
        this.receivedPacket = null;
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

            this.connection = new Connection(this, host, finalSeqNum, ackNum);
            this.logger.log(Level.INFO, "connection established with host: " + this.connection);
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

        this.connection = new Connection(this, node, seqNum, ackNum);
        this.logger.log(Level.INFO, "connection established with: " + this.connection);
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
                .withSequenceNumber(this.connection.getNextSequenceNumber())
                .withConnection(this.connection)
                .withPayload(payload)
                .build();
        send(packet);
    }

    @Override
    public Packet receive() {
        if (this.receivedPacket != null){
            Packet packet = this.receivedPacket;
            this.receivedPacket = null;
            return packet;
        }
        return null;
    }

    @Override
    public void close() {

    }

    protected abstract boolean isWaitingForACK();

    protected abstract void releaseWaitForAck();

    protected abstract void setWaitForAck();

    protected abstract boolean packetIsFromValidConnection(Packet packet);


    //todo - abstract
    public Connection getConnection() {
        if (this.connection == null) logger.log(Level.WARNING, "no connection established!");
        return this.connection;
    }

    //todo - abstract
    private void updateConnection(Packet packet){
        this.connection.update(packet);
    }

    private void ack(Packet packet){
        boolean isNextPacket = packet.getSequenceNumber() == this.connection.getNextAcknowledgementNumber();
        if (!isNextPacket){
            this.logger.log(Level.INFO, "the received packet was discarded due to arriving out of order");
            return;
        }
        this.connection.update(packet);
        this.receivedPacket = packet;
        this.send(new Packet.PacketBuilder()
                .withConnection(this.connection)
                .withFlags(Flag.ACK)
                .build()
        );
    }


    private void handleIncoming(){
        if (this.inputBufferIsEmpty()){
            sleep();
            return;
        }

        Packet packet = this.dequeueInputBuffer();
        System.out.println("packet: " + packet + " received");

        if (packetIsFromValidConnection(packet)){
            if (packet.hasFlag(Flag.ACK)){
                this.updateConnection(packet);
                this.releaseWaitForAck();
                return;
            }

            if (packet.hasFlag(Flag.FIN)){
                this.close();
                return;
            }
            this.ack(packet);

        }else if (packet.hasFlag(Flag.SYN)){
            this.connect(packet);
            return;
        }

    }


    private void trySend(){
        if (this.isWaitingForACK() || this.outputBufferIsEmpty()){
            this.sleep();
            return;
        }
        Packet packet = this.dequeueOutputBuffer();
        this.route(packet);
        if (packet.hasFlag(Flag.ACK)) return;

        this.setWaitForAck();
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
