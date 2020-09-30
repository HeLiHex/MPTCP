package org.example.network;

import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public abstract class Routable extends Thread implements NetworkNode {

    private RoutingTable routingTable;
    private List<NetworkNode> neighbours;

    protected Queue<Packet> inputBuffer;
    private Queue<Packet> outputBuffer;

    private Address address;
    private int cost;

    public Routable() {
        this.routingTable = new RoutingTable(this);;
        this.neighbours = new ArrayList<>();
        this.inputBuffer = new BufferQueue<>(100);
        this.outputBuffer = new BufferQueue<>(100);
        this.address = new Address();
        this.cost = 10;
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

    @Override
    public void deliverPackage(Packet packet) {
        if (!this.inputBuffer.offer(packet)) {
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
    public UUID getAddress() {
        return this.address.getAddress();
    }

    @Override
    public int getCost() {
        return this.cost;
    }

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
