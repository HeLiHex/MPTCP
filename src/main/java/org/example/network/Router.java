package org.example.network;


import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.PoissonDistribution;
import org.apache.commons.math.distribution.PoissonDistributionImpl;
import org.example.data.Packet;
import org.example.network.address.Address;
import org.example.network.address.UUIDAddress;
import org.example.simulator.statistics.RouterStats;
import org.example.util.Util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router extends Routable {

    private final RouterStats stats;
    private long interArrivalTime;
    private final int bufferSize;
    private final double queueUsage;
    private int artificialQueueSize;

    private Router(int bufferSize, double noiseTolerance, Address address) {
        super(new ArrayBlockingQueue<>(bufferSize), noiseTolerance, address);
        this.stats = new RouterStats(this);
        this.interArrivalTime = 0;
        this.bufferSize = bufferSize;
        this.queueUsage = 0.95;
        this.artificialQueueSize = getPoissonRandom(bufferSize*this.queueUsage);
    }

    @Override
    public RouterStats getStats(){
        return this.stats;
    }

    //source - https://stackoverflow.com/questions/9832919/generate-poisson-arrival-in-java
    private static int getPoissonRandom(double mean) {
        double L = Math.exp(-mean);
        int k = 0;
        double p = 1.0;
        do {
            p = p * Util.getNextRandomDouble();
            k++;
        } while (p > L);
        return k - 1;
    }

    @Override
    public long processingDelay() {
        return (super.processingDelay()*100 + this.artificialQueueSize);

    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        this.artificialQueueSize = getPoissonRandom(this.bufferSize*this.queueUsage);
        this.stats.packetArrival();
        if (this.artificialQueueSize + this.inputBufferSize() >= this.bufferSize) return false;
        return super.enqueueInputBuffer(packet);
    }

    @Override
    public void run() {
        this.artificialQueueSize = getPoissonRandom(this.bufferSize*this.queueUsage);
        if (!this.inputBufferIsEmpty()) {
            this.route(this.dequeueInputBuffer());
            this.stats.packetDeparture();
            return;
        }
        Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "this router has an empty buffer");
    }

    public static class RouterBuilder {

        private int bufferSize = 1000;
        private double noiseTolerance = 100.0;
        private Address address = new UUIDAddress();

        public RouterBuilder withBufferSize(int bufferSize) {
            //this.bufferSize = bufferSize;
            return this;
        }

        public RouterBuilder withNoiseTolerance(double noiseTolerance) {
            this.noiseTolerance = noiseTolerance;
            return this;
        }

        public RouterBuilder withAddress(Address address){
            this.address = address;
            return this;
        }

        public Router build() {
            return new Router(this.bufferSize, this.noiseTolerance, this.address);
        }

    }


}
