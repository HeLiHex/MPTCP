package org.example.network;

import org.example.data.Packet;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.Statistics;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Channel implements Comparable<Channel>{

    private Logger logger;
    private NetworkNode source;
    private NetworkNode destination;
    private int cost;
    private Random randomGenerator;
    private double noiseTolerance;

    public Channel(NetworkNode source, NetworkNode destination, Random randomGenerator, double noiseTolerance) {
        this.logger = Logger.getLogger(getClass().getName());

        this.source = source;
        this.destination = destination;
        this.cost = randomGenerator.nextInt(100);
        this.randomGenerator = randomGenerator;
        this.noiseTolerance = noiseTolerance;
    }

    public Channel(NetworkNode source){
        //loopback
        this.logger = Logger.getLogger(getClass().getName());
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


    public synchronized void channelPackage(Packet packet) {
        if (lossy()){
            Statistics.packetLost();
            this.logger.log(Level.INFO, () -> "Packet " + packet.toString() + " lost due to noise");
            return;
        }
        if (!this.destination.enqueueInputBuffer(packet)) {
            this.logger.log(Level.INFO, () -> "Packet " + packet.toString() + " was not delivered to " + this.destination);
        }
        //System.out.println("Channel " + this);w
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
