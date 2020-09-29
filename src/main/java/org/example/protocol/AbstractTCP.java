package org.example.protocol;

import org.example.Client;
import org.example.network.Address;
import org.example.network.NetworkNode;
import org.example.network.RoutingTable;
import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractTCP implements TCP, NetworkNode {

    private Logger logger = Logger.getLogger(Client.class.getName());

    private Queue<Packet> inputBuffer;
    private Queue<Packet> outputBuffer;
    private List<NetworkNode> neighbours;
    private final Address address;
    private RoutingTable routingTable;

    public AbstractTCP(int inputBufferSize, int outputBufferSize) {
        this.outputBuffer = new BufferQueue<>(outputBufferSize);
        this.inputBuffer = new BufferQueue<>(inputBufferSize);
        this.address = new Address();
        this.routingTable = new RoutingTable(this);
        this.neighbours = new ArrayList<>(1);
    }

    @Override
    public void connect() {

    }

    @Override
    public void send(Packet packet) {
        boolean wasAdded = this.outputBuffer.offer(packet);
        if (!wasAdded) {
            logger.log(Level.WARNING, "packet was not added to the output queue");
            return;
        }
    }


    @Override
    public Packet receive() {
        return this.inputBuffer.poll();
    }

    @Override
    public void close() {

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
    public int getCost() {
        return 0;
    }

    @Override
    public void updateRoutingTable() {
        this.routingTable.update(this);
        System.out.println(this.routingTable);
    }

    @Override
    public void route(Packet packet) {
        System.out.println(this.address.getAddress());
        NetworkNode destination = packet.getDestination();
        NetworkNode nextNodeOnPath = this.routingTable.getPath(this, destination);
        nextNodeOnPath.deliverPackage(packet);
    }

    @Override
    public void deliverPackage(Packet packet) {
        if (!this.inputBuffer.offer(packet)){
            System.out.println("Packet was not delivered to next NetworkNode");
        }
        System.out.println(this.inputBuffer.size());
    }

    @Override
    public UUID getAddress() {
        return this.address.getAddress();
    }

    @Override
    public String toString() {
        return this.address.toString();
    }

    @Override
    public int compareTo(NetworkNode networkNode) {
        return this.getCost();
    }

    public Queue<Packet> getOutputBuffer() {
        return this.outputBuffer;
    }
}
