package org.example.network;

import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public abstract class Routable extends Thread implements NetworkNode {

    private RoutingTable routingTable;
    private List<NetworkNode> neighbours;
    protected Queue<Packet> inputBuffer;
    protected Queue<Packet> outputBuffer;
    private Address address;
    private int cost;

    public Routable(BufferQueue<Packet> inputBuffer, BufferQueue<Packet> outputBuffer) {
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
        this.routingTable = new RoutingTable(this);;
        this.neighbours = new ArrayList<>();
        this.address = new Address();
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
        nextNodeOnPath.deliverPackage(packet);
    }


    //todo - rename
    @Override
    public void deliverPackage(Packet packet) {
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
