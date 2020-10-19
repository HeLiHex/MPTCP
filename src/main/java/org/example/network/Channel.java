package org.example.network;

import org.example.data.Packet;

import java.util.Random;

public class Channel implements Comparable<Channel>{

    private NetworkNode source;
    private NetworkNode destination;
    private int cost;
    private Random randomGenerator;
    private double noiseTolerance;

    public Channel(NetworkNode source, NetworkNode destination, Random randomGenerator, double noiseTolerance) {
        this.source = source;
        this.destination = destination;
        this.cost = 10;//randomGenerator.nextInt(100);
        this.randomGenerator = randomGenerator;
        this.noiseTolerance = noiseTolerance;
    }

    public Channel(NetworkNode source){
        this.source = source;
        this.destination = null;
        this.cost = 0;
        this.randomGenerator = null;
        this.noiseTolerance = 0;

    }

    private boolean lossy(){
        if (randomGenerator == null) return false;
        double gaussianNoise = this.randomGenerator.nextGaussian();
        double noise = Math.abs(gaussianNoise);
        return noise > this.noiseTolerance;
    }


    public synchronized void channelPackage(Packet packet) {
        if (lossy()) return;
        if (!this.destination.enqueueInputBuffer(packet)) {
            System.out.println("Packet was not deliveredm " + this.destination);
            return;
        }
        System.out.println("Channel " + this);
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
        return "[" + cost + "]";
        //return this.source.toString() + " - " +  this.destination.toString() + "["+ this.cost +"]";
    }

    @Override
    public int compareTo(Channel channel) {
        if (this.cost > channel.getCost()) return -1;
        if (this.cost < channel.getCost()) return 1;
        return 0;
    }
}
