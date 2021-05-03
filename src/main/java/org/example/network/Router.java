package org.example.network;

import org.apache.commons.math3.distribution.PoissonDistribution;
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
    private final double queueSizeMean;
    private final int bufferSize;
    private int artificialQueueSize;
    private final PoissonDistribution poissonDistribution;

    private Router(int bufferSize, double noiseTolerance, Address address) {
        super(new ArrayBlockingQueue<>(bufferSize), noiseTolerance, address);
        this.stats = new RouterStats(this);
        this.bufferSize = bufferSize;
        this.queueSizeMean = 0.7 * this.bufferSize;
        this.poissonDistribution = Util.getPoissonDistribution(this.queueSizeMean);
        this.artificialQueueSize = this.poissonDistribution.sample();
    }

    @Override
    public RouterStats getStats(){
        return this.stats;
    }


    @Override
    public long processingDelay() {
        return (super.processingDelay()*100 + this.artificialQueueSize);
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        this.stats.packetArrival();
        if (this.artificialQueueSize + this.inputBufferSize() >= this.bufferSize) return false;
        return super.enqueueInputBuffer(packet);
    }

    private void arrivalAndServiceProcess(){
        this.artificialQueueSize = poissonDistribution.sample();
        if (this.artificialQueueSize > this.bufferSize) this.artificialQueueSize = bufferSize;
        if (this.artificialQueueSize < 0) this.artificialQueueSize = 0;
        System.out.println(this.artificialQueueSize);
        this.stats.trackQueueSize(this.artificialQueueSize);
    }

    @Override
    public void run() {
        this.arrivalAndServiceProcess();
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
