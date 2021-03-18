package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.Statistics;
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
    private final double noiseTolerance;
    private final int capacity = 100;
    boolean goodState = true;

    private Channel(NetworkNode source, NetworkNode destination, double noiseTolerance, int cost) {
        this.logger = Logger.getLogger(getClass().getSimpleName());
        this.line = new ArrayBlockingQueue<>(capacity);

        this.source = source;
        this.destination = destination;
        this.noiseTolerance = noiseTolerance;
        this.cost = cost;
    }

    public Channel(NetworkNode source, NetworkNode destination, double noiseTolerance) {
        this(source, destination, noiseTolerance, Util.getNextRandomInt(100));
    }

    //loopback
    public Channel(NetworkNode source) {
        this(source, source, 0, 0);
    }

    public long propagationDelay() {
        return this.cost / 2;
    }

    private boolean lossy() {
        if (this.source.equals(this.destination)) return false;
        double gaussianNoise = Util.getNextGaussian();
        double noise = StrictMath.abs(gaussianNoise);
        if (goodState) {
            this.goodState = Util.getNextRandomInt(100) >= 10;
            return noise > this.noiseTolerance + 2;
        }
        this.goodState = Util.getNextRandomInt(100) >= 50;
        return noise > this.noiseTolerance;
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

        Packet packet = this.line.poll();
        if (lossy()) {
            Statistics.packetLost();
            this.logger.log(Level.INFO, () -> packet.toString() + " lost due to noise");
            return false;
        }

        boolean sendSuccess = this.destination.enqueueInputBuffer(packet);
        if (!sendSuccess) {
            Statistics.packetDropped();
            this.logger.log(Level.INFO, () -> packet.toString() + " was not delivered to " + this.destination);
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
}
