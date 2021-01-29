package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.Statistics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Channel implements Comparable<Channel>{

    private final Logger logger;

    private final Queue<Packet> line;
    private final NetworkNode source;
    private final NetworkNode destination;
    private final int cost;
    private final Random randomGenerator;
    private final double noiseTolerance;

    public Channel(NetworkNode source, NetworkNode destination, Random randomGenerator, double noiseTolerance) {
        this.logger = Logger.getLogger(getClass().getSimpleName());
        this.line = new ArrayDeque<>();
        this.source = source;
        this.destination = destination;
        this.cost = randomGenerator.nextInt(100);
        this.randomGenerator = randomGenerator;
        this.noiseTolerance = noiseTolerance;
    }

    public Channel(NetworkNode source){
        //loopback
        this.logger = Logger.getLogger(getClass().getSimpleName());
        this.line = new ArrayDeque<>();
        this.source = source;
        this.destination = source;
        this.cost = 0;
        this.randomGenerator = null;
        this.noiseTolerance = 0;
    }

    private synchronized boolean lossy(){
        if (randomGenerator == null) return false;
        double gaussianNoise = this.randomGenerator.nextGaussian();
        double noise = Math.abs(gaussianNoise);
        return noise > this.noiseTolerance;
    }


    public void channelPackage(Packet packet) {
        this.line.add(packet);
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

    public boolean channel(){
        Packet packet = this.line.poll();
        if (packet == null){
            System.out.println("wtf");
            return false;
        }
        if (lossy()){
            Statistics.packetLost();
            this.logger.log(Level.INFO, () -> "Packet " + packet.toString() + " lost due to noise");
            return false;
        }

        boolean sendSuccess = this.destination.enqueueInputBuffer(packet);
        if (!sendSuccess) {
            Statistics.packetDropped();
            this.logger.log(Level.INFO, () -> "Packet " + packet.toString() + " was not delivered to " + this.destination);
            return false;
        }
        return true;
        //System.out.println("Channel " + this);w
    }

    @Override
    public String toString() {
        return this.source.toString() + " -> [" + this.cost + "] -> " +  this.destination.toString();
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
