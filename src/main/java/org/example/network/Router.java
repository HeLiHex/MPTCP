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

    private Router(int bufferSize, Address address, double averageQueueUtilization) {
        super(new ArrayBlockingQueue<>(bufferSize), address);
        this.stats = new RouterStats(this);
        this.bufferSize = bufferSize;
        this.queueSizeMean = averageQueueUtilization * this.bufferSize;
        this.poissonDistribution = Util.getPoissonDistribution(this.queueSizeMean);
        this.setArtificialQueueSize();
    }

    @Override
    public RouterStats getStats(){
        return this.stats;
    }

    private void setArtificialQueueSize(){
        int queueSize = this.poissonDistribution.sample();// + this.inputBufferSize();
        //if (queueSize > this.bufferSize) queueSize = this.bufferSize;
        //if (queueSize < 0) queueSize = 0;

        this.artificialQueueSize = queueSize;

        this.stats.trackQueueSize(this.artificialQueueSize);

    }


    @Override
    public long delay() {
        this.setArtificialQueueSize();
        long transmissionDelay = 10;
        long delay = transmissionDelay + this.artificialQueueSize * transmissionDelay;
        //System.out.println("router delay: " + delay);
        return delay;
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        //this.setArtificialQueueSize();
        this.stats.packetArrival(packet);
        /*
        if (this.artificialQueueSize >= this.bufferSize){
            //packets dropped due to full buffer
            System.out.println("packet dropped");
            return false;
        }

         */

        boolean success = super.enqueueInputBuffer(packet);
        return success;
    }

    @Override
    public void run() {
        if (!this.inputBufferIsEmpty()) {
            Packet packet = this.dequeueInputBuffer();
            this.route(packet);
            //this.stats.packetDeparture(packet);
            return;
        }
        Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "this router has an empty buffer");
    }

    public static class RouterBuilder {

        private int bufferSize = 100;
        private double averageQueueUtilization = 0.85;
        private Address address = new UUIDAddress();

        public RouterBuilder withAverageQueueUtilization(double averageQueueUtilization) {
            this.averageQueueUtilization = averageQueueUtilization;
            return this;
        }

        public RouterBuilder withAddress(Address address){
            this.address = address;
            return this;
        }

        public Router build() {
            return new Router(this.bufferSize, this.address, this.averageQueueUtilization);
        }

    }


}
