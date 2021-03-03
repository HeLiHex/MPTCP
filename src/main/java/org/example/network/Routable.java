package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.NetworkNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class Routable implements NetworkNode {

    private final RoutingTable routingTable;
    private final List<Channel> channels;
    protected BlockingQueue<Packet> inputBuffer;
    private final Address address;
    private final double noiseTolerance;

    protected Routable(BlockingQueue<Packet> inputBuffer, double noiseTolerance) {
        this.inputBuffer = inputBuffer;
        this.channels = new ArrayList<>();
        this.address = new Address();
        this.noiseTolerance = noiseTolerance;
        this.routingTable = new RoutingTable();
    }

    @Override
    public void updateRoutingTable() {
        this.routingTable.update(this);
    }

    @Override
    public void route(Packet packet) {
        if (packet == null) throw new IllegalStateException("Null packet can't be routed");
        //System.out.println("packet: " + packet + " is routed through router: " + this.address);
        NetworkNode destination = packet.getDestination();
        Channel nextChannelOnPath = this.routingTable.getPath(this, destination);
        nextChannelOnPath.channelPackage(packet);
    }

    @Override
    public Channel getPath(NetworkNode destination) {
        return this.routingTable.getPath(this, destination);
    }

    @Override
    public long processingDelay() {
        return 2;
    }

    @Override
    public List<Channel> getChannels() {
        return this.channels;
    }

    @Override
    public void addChannel(NetworkNode node) {
        for (Channel channel : this.getChannels()) {
            boolean thisContainsNode = channel.getDestination().equals(node);
            if (thisContainsNode) return;
        }
        Channel channel = new Channel(this, node, this.noiseTolerance);
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
    public Packet peekInputBuffer() {
        return this.inputBuffer.peek();
    }

    @Override
    public Packet dequeueInputBuffer() {
        return this.inputBuffer.poll();
    }

    @Override
    public boolean inputBufferIsEmpty() {
        return this.inputBuffer.isEmpty();
    }

    public int inputBufferSize() {
        return this.inputBuffer.size();
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


}
