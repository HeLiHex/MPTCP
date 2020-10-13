package org.example.protocol;

import org.example.data.Flag;
import org.example.network.Endpoint;
import org.example.network.NetworkNode;
import org.example.network.Routable;
import org.example.data.BufferQueue;
import org.example.data.Packet;
import org.example.network.RoutableEndpoint;


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractTCP extends RoutableEndpoint implements TCP, Endpoint {

    private Logger logger = Logger.getLogger(AbstractTCP.class.getName());


    //todo - probably ok with boolean, but locking and releasing should be determined by a abstract method
    private boolean waitingForACK;

    private NetworkNode connectedNode;
    private Packet receivedPacket;


    public AbstractTCP(BufferQueue<Packet> inputBuffer, BufferQueue<Packet> outputBuffer, Random randomGenerator, double noiseTolerance) {
        super(inputBuffer, outputBuffer, randomGenerator, noiseTolerance);
        this.waitingForACK = false;
        this.receivedPacket = null;
    }

    public NetworkNode getConnectedNode() {
        return connectedNode;
    }

    @Override
    public void connect(NetworkNode host) {
        this.updateRoutingTable();
        Packet syn = new Packet.PacketBuilder()
                .withDestination(host)
                .withOrigin(this)
                .withFlags(Flag.SYN)
                .build();
        this.route(syn);

        while (this.inputBufferIsEmpty()){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Thread Interrupted!");
                Thread.currentThread().interrupt();
            }
        }

        Packet synAck = this.dequeueInputBuffer();
        if (synAck.hasFlag(Flag.SYN, Flag.ACK) /*&& synAck.hasFlag(Flag.ACK)*/){
            System.out.println(synAck);
            Packet ack = new Packet.PacketBuilder()
                    .withDestination(host)
                    .withOrigin(this)
                    .withFlags(Flag.ACK)
                    .build();
            this.route(ack);

            this.connectedNode = host;
            this.logger.log(Level.INFO, "connection established with host: " + this.connectedNode);
            this.start();
        }

    }

    public void incomingConnect(Packet syn){
        NetworkNode node = syn.getOrigin();
        Packet synAck = new Packet.PacketBuilder()
                .withDestination(node)
                .withOrigin(this)
                .withFlags(Flag.SYN, Flag.ACK)
                .build();
        this.route(synAck);

        this.connectedNode = node;
        this.logger.log(Level.INFO, "connection established with: " + this.connectedNode);
    }

    @Override
    public void send(Packet packet) {
        if (!packet.getDestination().equals(this.connectedNode)){
            logger.log(Level.WARNING, "A connection must be established to send packets");
            System.out.println(this.connectedNode);
            return;
        }

        packet.setOrigin(this);
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
        Packet packet = this.dequeueInputBuffer();
        if (packet == null) return;

        System.out.println("packet: " + packet + " received");


        if (packet.hasFlag(Flag.ACK) && !packet.hasFlag(Flag.SYN)){
            this.waitingForACK = false;
            return;
        }
        if (packet.hasFlag(Flag.SYN) && !packet.hasFlag(Flag.ACK)){
            incomingConnect(packet);
            return;
        }
        if (packet.hasFlag(Flag.FIN)){
            close();
            return;
        }

        this.receivedPacket = packet;
        this.send(new Packet.PacketBuilder()
                .withOrigin(this)
                .withDestination(packet.getOrigin())
                .withFlags(Flag.ACK)
                .build()
        );
    }

    private void trySend(){
        if (waitingForACK){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Thread Interrupted!");
                Thread.currentThread().interrupt();
            }
            //System.out.println("waiting for ack");
            return;
        }
        if (!this.outputBufferIsEmpty()){
            Packet packet = this.dequeueOutputBuffer();
            this.route(packet);
            this.waitingForACK = true;
        }
    }

    @Override
    public void run() {
        while (true){
            if (!this.inputBufferIsEmpty()){
                this.handleIncoming();
            }
            trySend();
        }
    }


}
