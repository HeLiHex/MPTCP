package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.MPTCP;
import org.example.util.Util;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Channel implements Comparable<Channel> {

    private static final double BAD_TO_GOOD_PROB = 0.2;
    private static final int CAPACITY = 1000;
    private final Queue<Packet> line;
    private final NetworkNode source;
    private final NetworkNode destination;
    private final int cost;
    private final double loss;
    private boolean goodState;


    public Channel(NetworkNode source, NetworkNode destination, double loss, int cost) {
        this.line = new ArrayBlockingQueue<>(CAPACITY);
        this.source = source;
        this.destination = destination;
        this.loss = loss;
        this.cost = cost;
        this.goodState = true;
    }

    public Channel(NetworkNode source, NetworkNode destination, double loss) {
        this(source, destination, loss, Util.getNextRandomInt(100));
    }

    //loopback
    public Channel(NetworkNode source) {
        this(source, source, 0, 0);
    }

    public long propagationDelay() {
        return 10 * (this.cost + 1L);
    }

    private boolean lossy() {
        if (this.source.equals(this.destination)) return false;
        if (this.goodState) {
            this.goodState = Util.getNextRandomDouble() >= this.loss;
            return Util.getNextRandomDouble() < this.loss;
        }
        this.goodState = Util.getNextRandomDouble() >= BAD_TO_GOOD_PROB;
        return Util.getNextRandomDouble() < BAD_TO_GOOD_PROB;
    }

    public void channelPackage(Packet packet) {
        if (!this.line.offer(packet)) {
            throw new IllegalStateException("packet dropped in channel");
        }
    }

    public NetworkNode getSource() {
        return source;
    }

    public NetworkNode getDestination() {
        return destination;
    }

    public int getCost() {
        return cost;
    }

    public boolean channel() {
        if (this.line.isEmpty()) {
            return false;
        }

        var packet = this.line.poll();
        if (lossy()) {
            return false;
        }
        return this.destination.enqueueInputBuffer(packet);

    }

    @Override
    public String toString() {
        return this.source.toString() + " -> [" + this.cost + "] -> " + this.destination.toString();
    }

    @Override
    public int compareTo(Channel channel) {
        if (this.cost > channel.getCost()) return 1;
        if (this.cost < channel.getCost()) return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public static class ChannelBuilder {

        double loss = 0;
        int cost = Util.getNextRandomInt(100);

        public ChannelBuilder withCost(int cost) {
            if (cost < 0 || cost > 100) throw new IllegalStateException("cost is not valid");
            this.cost = cost;
            return this;
        }

        public ChannelBuilder withLoss(double loss) {
            this.loss = loss;
            return this;
        }

        public void build(NetworkNode node1, NetworkNode node2) {
            if (node1 instanceof MPTCP) node1 = ((MPTCP) node1).getEndpointToAddChannelTo();
            if (node2 instanceof MPTCP) node2 = ((MPTCP) node2).getEndpointToAddChannelTo();
            node1.addChannel(node2, this.loss, this.cost);
            node2.addChannel(node1, this.loss, this.cost);
        }

    }

}
