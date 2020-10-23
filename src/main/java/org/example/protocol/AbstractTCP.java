package org.example.protocol;

import org.example.data.Flag;
import org.example.network.interfaces.NetworkNode;
import org.example.data.BufferQueue;
import org.example.data.Packet;
import org.example.network.RoutableEndpoint;


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTCP extends RoutableEndpoint implements TCP {

    private final Logger logger = Logger.getLogger(AbstractTCP.class.getName());


    //todo - probably ok with boolean, but locking and releasing should be determined by a abstract method
    private boolean waitingForACK;
    private Connection connection;
    private Packet receivedPacket;


    public AbstractTCP(BufferQueue<Packet> inputBuffer, BufferQueue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
        super(inputBuffer, outputBuffer, randomGenerator, noiseTolerance);
        this.waitingForACK = false;
        this.receivedPacket = null;
    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void connect(NetworkNode host) {
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
        NetworkNode node = syn.getOrigin();
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
        //todo - dette er ikke sant. dette håndteres hos mottaker
        if (!packet.getDestination().equals(this.connection.getConnectedNode())){
            logger.log(Level.WARNING, "A connection must be established to send packets");
            System.out.println(this.connection);
            return;
        }

        boolean wasAdded = this.enqueueOutputBuffer(packet);
        if (!wasAdded) {
            logger.log(Level.WARNING, "Packet was not added to the output queue");
            return;
        }
        System.out.println("packet: " + packet + " sent");
    }

    @Override
    public Packet receive() {
        //todo - hand over to socket/client
        return this.receivedPacket;
    }

    @Override
    public void close() {

    }


    private synchronized void handleIncoming(){
        if (this.inputBufferIsEmpty()){
            sleep();
            return;
        }

        Packet packet = this.dequeueInputBuffer();
        System.out.println("packet: " + packet + " received");

        //TODO - dette kan gjøres bedre
        if (packet.hasFlag(Flag.ACK) && !packet.hasFlag(Flag.SYN)){
            this.waitingForACK = false;
            return;
        }
        if (packet.hasFlag(Flag.SYN) && !packet.hasFlag(Flag.ACK)){
            connect(packet);
            return;
        }
        if (packet.hasFlag(Flag.FIN)){
            close();
            return;
        }

        this.connection.update(packet);
        this.receivedPacket = packet;
        this.send(new Packet.PacketBuilder()
                .withOrigin(this)
                .withDestination(packet.getOrigin())
                .withFlags(Flag.ACK)
                .withSequenceNumber(this.connection.getNextSequenceNumber())
                .withAcknowledgmentNumber(this.connection.getNextAcknowledgementNumber())
                .build()
        );
    }

    private void trySend(){
        if (this.waitingForACK || this.outputBufferIsEmpty()){
            this.sleep();
            return;
        }
        Packet packet = this.dequeueOutputBuffer();
        this.route(packet);
        this.waitingForACK = true;
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
