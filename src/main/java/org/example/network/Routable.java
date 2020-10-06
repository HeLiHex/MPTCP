package org.example.network;

import org.example.data.BufferQueue;
import org.example.data.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public abstract class Routable extends Thread implements NetworkNode {

    private RoutingTable routingTable;
    private List<NetworkNode> neighbours;
    protected Queue<Packet> inputBuffer;
    protected Queue<Packet> outputBuffer;
    private Address address;
    private int cost;
    private Random randomGenerator;
    private double noiseTolerance;

    public Routable(BufferQueue<Packet> inputBuffer, BufferQueue<Packet> outputBuffer, Random randomGenerator) {
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
        this.routingTable = new RoutingTable(this);;
        this.neighbours = new ArrayList<>();
        this.address = new Address();
        this.randomGenerator = randomGenerator;
        this.noiseTolerance = 100.0;
        setRandomCost();
    }

    @Override
    public void updateRoutingTable() {
        this.routingTable.update(this);
        System.out.println(this.routingTable);
    }

    @Override
    public void route(Packet packet) {
        System.out.println("packet: " + packet + " is routed through router: " + this.address);
        NetworkNode destination = packet.getDestination();
        NetworkNode nextNodeOnPath = this.routingTable.getPath(this, destination);
        nextNodeOnPath.channelPackage(packet);
    }

    private boolean lossy(){
        double gaussianNoise = this.randomGenerator.nextGaussian();
        double noise = Math.abs(gaussianNoise);
        return noise > this.noiseTolerance;
    }


    //todo - rename
    @Override
    public void channelPackage(Packet packet) {
        if (lossy()) return;
        if (!this.enqueueInputBuffer(packet)) {
            System.out.println("Packet was not delivered to next NetworkNode");
        }
    }

    @Override
    public List<NetworkNode> getNeighbours() {
        return this.neighbours;
    }

    @Override
    public void addNeighbour(NetworkNode node) {
        if (!this.neighbours.contains(node)) {
            this.neighbours.add(node);
            node.getNeighbours().add(this);
            return;
        }
        System.out.println("Node is already added as neighbour");
    }

    @Override
    public Address getAddress() {
        return this.address;
    }

    @Override
    public int getCost() {
        return this.cost;
    }

    private void setRandomCost(){
        this.cost = (int) ((Math.random() + 1) * 10);
    }

    @Override
    public abstract boolean enqueueInputBuffer(Packet packet);

    @Override
    public abstract Packet dequeueInputBuffer();

    @Override
    public abstract boolean inputQueueIsEmpty();

    @Override
    public abstract Packet dequeueOutputBuffer();

    @Override
    public abstract boolean enqueueOutputBuffer(Packet packet);

    @Override
    public abstract boolean outputQueueIsEmpty();

    @Override
    public int compareTo(NetworkNode networkNode) {
        return this.getCost();
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
    public abstract void run();
}
