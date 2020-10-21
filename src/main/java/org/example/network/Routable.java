package org.example.network;

import org.example.data.BufferQueue;
import org.example.data.Packet;
import org.example.network.interfaces.NetworkNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public abstract class Routable extends Thread implements NetworkNode {

    private RoutingTable routingTable;
    private List<NetworkNode> neighbours;
    private List<Channel> channels;
    protected Queue<Packet> inputBuffer;
    private Address address;
    private Random randomGenerator;
    private double noiseTolerance;

    public Routable(BufferQueue<Packet> inputBuffer, Random randomGenerator, double noiseTolerance) {
        this.inputBuffer = inputBuffer;
        this.neighbours = new ArrayList<>();
        this.channels = new ArrayList<>();
        this.address = new Address();
        this.randomGenerator = randomGenerator;
        this.noiseTolerance = noiseTolerance;
        this.routingTable = new RoutingTable(this);
    }

    @Override
    public void updateRoutingTable() {
        this.routingTable.update(this);
    }

    @Override
    public void route(Packet packet) {
        processingDelay();
        //System.out.println("packet: " + packet + " is routed through router: " + this.address);
        NetworkNode destination = packet.getDestination();
        Channel nextChannelOnPath = this.routingTable.getPath(this, destination);
        nextChannelOnPath.channelPackage(packet);
    }

    @Override
    public void processingDelay(){
        try {
            sleep(Math.round(Math.abs(randomGenerator.nextGaussian()) * this.channels.size()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Channel> getChannels(){
        return this.channels;
    }

    @Override
    public void addChannel(NetworkNode node) {
        for (Channel channel : this.getChannels()){
            boolean thisContainsNode = channel.getDestination().equals(node);
            if (thisContainsNode) return;
        }
        Channel channel = new Channel(this, node, this.randomGenerator, this.noiseTolerance);
        this.channels.add(channel);
        node.addChannel(this);
    }

    @Override
    public Address getAddress() {
        return this.address;
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        return this.inputBuffer.offer(packet);
    }

    @Override
    public Packet dequeueInputBuffer() {
        return this.inputBuffer.poll();
    }

    @Override
    public boolean inputBufferIsEmpty() {
        return this.inputBuffer.isEmpty();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NetworkNode) {
            NetworkNode node = (NetworkNode) obj;
            return this.getAddress().equals(node.getAddress());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getAddress().hashCode();
    }

    @Override
    public String toString() {
        return this.address.toString();
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public abstract void run();
}
