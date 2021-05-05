package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.MPTCP;
import org.example.simulator.statistics.Statistics;
import org.example.util.Util;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Channel implements Comparable<Channel> {

    private final Logger logger;
    private final Queue<Packet> line;
    private final NetworkNode source;
    private final NetworkNode destination;
    private final int cost;
    private final double loss;
    private final int capacity = 1000;
    private boolean goodState;


    public Channel(NetworkNode source, NetworkNode destination, double loss, int cost) {
        this.logger = Logger.getLogger(getClass().getSimpleName());
        this.line = new ArrayBlockingQueue<>(capacity);

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

    public long transmissionDelay() {
        long delay = 100*this.cost;
        //System.out.println("channel delay: " + delay );
        return delay;
    }

    private boolean lossy() {
        if (this.source.equals(this.destination)) return false;
        if (this.goodState) {
            this.goodState = Util.getNextRandomDouble() >= this.loss;
        }else{
            this.goodState = Util.getNextRandomDouble() >= 0.4;
        }
        return !this.goodState;

        //return Util.getNextRandomDouble() < this.loss;
    }

    public void channelPackage(Packet packet) {
        if (!this.line.offer(packet)) {
            this.logger.log(Level.INFO, () -> packet.toString() + " lost due to channel capacity");
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
        //channel is in some cases called without the packet making it's way to the line
        //this prevents NullPointerException
        if (this.line.isEmpty()) {
            return false;
        }

        int size = this.line.size();
        var packet = this.line.poll();
        assert this.line.size() < size : "no packet removed";
        if (lossy()) {
            Statistics.packetLost();
            this.logger.log(Level.INFO, () -> packet.toString() + " lost due to noise");
            return false;
        }

        boolean sendSuccess = this.destination.enqueueInputBuffer(packet);
        if (!sendSuccess) {
            this.logger.log(Level.INFO, () -> packet.toString() + " was not delivered to " + this.destination);
            Statistics.packetDropped();
            return false;
        }
        return true;
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

        public ChannelBuilder withCost (int cost) {
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
