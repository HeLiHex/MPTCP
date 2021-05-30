package org.example.network;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.example.data.Packet;
import org.example.network.address.Address;
import org.example.network.address.UUIDAddress;
import org.example.simulator.statistics.Stats;
import org.example.util.Util;

import java.util.concurrent.ArrayBlockingQueue;

public class Router extends Routable {

    private final double queueSizeMean;
    private final int bufferSize;
    private final PoissonDistribution poissonDistribution;
    private int artificialQueueSize;

    private Router(int bufferSize, Address address, double averageQueueUtilization) {
        super(new ArrayBlockingQueue<>(bufferSize), address);
        this.bufferSize = bufferSize;
        this.queueSizeMean = averageQueueUtilization * this.bufferSize;
        this.poissonDistribution = Util.getPoissonDistribution(this.queueSizeMean);
        this.setArtificialQueueSize();
    }

    @Override
    public Stats getStats() {
        return null;
    }

    private void setArtificialQueueSize() {
        int queueSize = this.poissonDistribution.sample();
        this.artificialQueueSize = queueSize;
    }

    @Override
    public long delay() {
        this.setArtificialQueueSize();
        long transmissionDelay = 10;
        return transmissionDelay + this.artificialQueueSize * transmissionDelay;
    }

    @Override
    public void run() {
        if (!this.inputBufferIsEmpty()) {
            var packet = this.dequeueInputBuffer();
            this.route(packet);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static class RouterBuilder {

        private int bufferSize = 100;
        private double averageQueueUtilization = 0.85;
        private Address address = new UUIDAddress();

        public RouterBuilder withAverageQueueUtilization(double averageQueueUtilization) {
            this.averageQueueUtilization = averageQueueUtilization;
            return this;
        }

        public RouterBuilder withAddress(Address address) {
            this.address = address;
            return this;
        }

        public Router build() {
            return new Router(this.bufferSize, this.address, this.averageQueueUtilization);
        }

    }


}
