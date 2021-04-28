package org.example.network;

import org.example.data.Packet;
import org.example.network.address.Address;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.MPTCP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class Routable implements NetworkNode {

    protected final BlockingQueue<Packet> inputBuffer;
    private final RoutingTable routingTable;
    private final List<Channel> channels;
    private final Address address;
    private final double noiseTolerance;
    private List<Channel> channelsUsed;

    protected Routable(BlockingQueue<Packet> inputBuffer, double noiseTolerance, Address address) {
        this.inputBuffer = inputBuffer;
        this.channels = new ArrayList<>();
        this.address = address;
        this.noiseTolerance = noiseTolerance;
        this.routingTable = new RoutingTable();

        this.channelsUsed = new ArrayList<>(1);
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

        //assert channelsUsed.size() < 1;
        this.channelsUsed.add(nextChannelOnPath);
    }

    private Channel getPath(NetworkNode destination) {
        return this.routingTable.getPath(this, destination);
    }

    @Override
    public List<Channel> getChannelsUsed() {
        List<Channel> used = this.channelsUsed;
        this.channelsUsed = new ArrayList<>(1);
        return used;
    }

    @Override
    public long processingDelay() {
        return ((long) this.inputBufferSize()) * 10;
    }

    @Override
    public List<Channel> getChannels() {
        return this.channels;
    }

    @Override
    public void addChannel(NetworkNode node) {
        if (node instanceof MPTCP) {
            MPTCP mptcp = (MPTCP) node;
            node = mptcp.getEndpointToAddChannelTo();
        }

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

    @Override
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
