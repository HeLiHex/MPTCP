package org.example.network;


import org.example.data.Packet;
import org.example.network.address.Address;
import org.example.network.address.UUIDAddress;
import org.example.simulator.statistics.RouterStats;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router extends Routable {

    private final RouterStats stats;

    private Router(int bufferSize, double noiseTolerance, Address address) {
        super(new ArrayBlockingQueue<>(bufferSize), noiseTolerance, address);
        this.stats = new RouterStats(this);
    }

    @Override
    public RouterStats getStats(){
        return this.stats;
    }

    @Override
    public long processingDelay() {
        return super.processingDelay()*100;
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        this.stats.packetArrival();
        return super.enqueueInputBuffer(packet);
    }

    @Override
    public void run() {
        if (!this.inputBufferIsEmpty()) {
            this.route(this.dequeueInputBuffer());
            this.stats.packetDeparture();
            return;
        }
        Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "this router has an empty buffer");
    }

    public static class RouterBuilder {

        private int bufferSize = 100;
        private double noiseTolerance = 100.0;
        private Address address = new UUIDAddress();

        public RouterBuilder withBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
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
