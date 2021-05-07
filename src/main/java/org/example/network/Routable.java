package org.example.network;

import org.example.data.Packet;
import org.example.network.address.Address;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.MPTCP;
import org.example.simulator.statistics.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class Routable implements NetworkNode {

    protected final BlockingQueue<Packet> inputBuffer;
    private final RoutingTable routingTable;
    private final List<Channel> channels;
    private final Address address;
    private List<Channel> channelsUsed;

    protected Routable(BlockingQueue<Packet> inputBuffer, Address address) {
        this.inputBuffer = inputBuffer;
        this.channels = new ArrayList<>();
        this.address = address;
        this.routingTable = new RoutingTable();

        this.channelsUsed = new ArrayList<>(1);
    }

    public abstract Stats getStats();

    @Override
    public void updateRoutingTable() {
        this.routingTable.update(this);
    }

    @Override
    public void route(Packet packet) {
        if (packet == null) throw new IllegalStateException("Null packet can't be routed");
        //System.out.println("packet: " + packet + " is routed through router: " + this.address);
        NetworkNode destination = packet.getDestination();
        var nextChannelOnPath = this.routingTable.getPath(this, destination);
        nextChannelOnPath.channelPackage(packet);

        this.channelsUsed.add(nextChannelOnPath);
    }

    @Override
    public List<Channel> getChannelsUsed() {
        List<Channel> used = this.channelsUsed;
        this.channelsUsed = new ArrayList<>(1);
        return used;
    }

    @Override
    public long delay() {
        return ((long) this.inputBufferSize()) * 10;
    }

    @Override
    public List<Channel> getChannels() {
        return this.channels;
    }


    @Override
    public void addChannel(NetworkNode node, double noiseTolerance, int cost) {
        var channel = new Channel(this, node, noiseTolerance, cost);
        this.channels.add(channel);
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
