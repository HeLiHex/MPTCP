package org.example.network;

import org.example.protocol.util.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class Router extends Thread implements NetworkNode{

    private RoutingTable routingTable;
    private List<NetworkNode> neighbours;

    private Queue<Packet> inputBuffer;
    private Queue<Packet> outputBuffer;

    private Address address;

    private int cost;

    public Router() {
        this.routingTable = new RoutingTable(this);
        this.address = new Address(UUID.randomUUID().toString(), 10);
        this.neighbours = new ArrayList<>();
        this.cost = 10;
    }

    @Override
    public List<NetworkNode> getNeighbours() {
        return this.neighbours;
    }

    @Override
    public void addNeighbour(NetworkNode node){
        if (!this.neighbours.contains(node)){
            this.neighbours.add(node);
            node.getNeighbours().add(this);
            return;
        }
        System.out.println("Node is already added as neighbour");
    }

    @Override
    public int getCost() {
        return this.cost;
    }

    @Override
    public NetworkNode getPath(NetworkNode destination) {
        return this.routingTable.getPath(destination);
    }

    @Override
    public void updateRoutingTable() {
        this.routingTable.update(this);
        System.out.println(this.routingTable);
    }

    @Override
    public String toString() {
        return address.getAddress();
    }

    @Override
    public void route(Packet packet) {

    }
}
