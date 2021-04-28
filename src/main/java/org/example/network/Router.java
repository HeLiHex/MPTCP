package org.example.network;


import org.example.network.address.Address;
import org.example.network.address.UUIDAddress;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router extends Routable {

    private Router(int bufferSize, double noiseTolerance, Address address) {
        super(new ArrayBlockingQueue<>(bufferSize), noiseTolerance, address);
    }

    @Override
    public void run() {
        if (!this.inputBufferIsEmpty()) {
            this.route(this.dequeueInputBuffer());
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
