package org.example.protocol;

import org.example.Client;
import org.example.network.Address;
import org.example.network.NetworkNode;
import org.example.network.Router;
import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractTCP implements TCP, NetworkNode{

    private Logger logger = Logger.getLogger(Client.class.getName());

    private Queue<Packet> inputBuffer;
    private Queue<Packet> outputBuffer;
    private Router router;

    public AbstractTCP(int inputBufferSize, int outputBufferSize) {
        this.outputBuffer = new BufferQueue<>(outputBufferSize);
        this.inputBuffer = new BufferQueue<>(inputBufferSize);
        this.router = new Router();
    }

    @Override
    public void connect() {

    }

    @Override
    public void send(Packet packet) {
        boolean wasAdded = this.outputBuffer.offer(packet);
        if (!wasAdded){
            logger.log(Level.WARNING, "packet was not added to the output queue");
            return;
        }
    }


    @Override
    public Packet receive() {
        return null;
    }

    @Override
    public void close() {

    }


    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public List<NetworkNode> getNeighbours() {
        List<NetworkNode> router = new ArrayList<>(1);
        router.add(this.router);
        return router;
    }

    @Override
    public void addNeighbour(NetworkNode node) {

    }

    @Override
    public int getCost() {
        return 0;
    }

    @Override
    public NetworkNode getPath(NetworkNode destination) {
        return null;
    }

    @Override
    public void updateRoutingTable() {

    }


    public Queue<Packet> getOutputBuffer() {
        return this.outputBuffer;
    }
}
