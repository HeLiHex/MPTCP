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
    Queue ackChannelTMP;

    private int cost;

    public Router() {
        this.routingTable = new RoutingTable(this);
        this.address = new Address(UUID.randomUUID().toString(), 10);
        this.neighbours = new ArrayList<>();
        this.cost = 10;
    }


    //tmp
    public Router(Queue ackChannelTMP) {
        this.routingTable = new RoutingTable(this);
        this.address = new Address(UUID.randomUUID().toString(), 10);
        this.ackChannelTMP = ackChannelTMP;
        this.neighbours = new ArrayList<>();
        this.cost = 10;
    }

    @Override
    public Address getAddress() {
        return this.address;
    }

    @Override
    public List<NetworkNode> getNeighbours() {
        return this.neighbours;
    }

    @Override
    public void addNeighbour(NetworkNode node){
        if (!this.neighbours.contains(node)){
            System.out.println("hello"); //todo something weird
            this.neighbours.add(node);
            node.getNeighbours().add(this);
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

    public void route(Packet packet) {
        NetworkNode destination = packet.getDestination();
        this.routingTable.getPath(destination);
        System.out.println("Packet sent");
        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ackChannelTMP.offer(new Packet("ACK"));
    }

    public void update(){
        this.routingTable.updateTable(this);
        System.out.println(this.routingTable);
    }

    @Override
    public String toString() {
        return address.getAddress();
    }
}
