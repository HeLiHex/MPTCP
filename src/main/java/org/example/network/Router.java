package org.example.network;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router extends Routable {

    private Router(int bufferSize, double noiseTolerance) {
        super(new ArrayBlockingQueue<>(bufferSize), noiseTolerance);
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

        public RouterBuilder withBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public RouterBuilder withNoiseTolerance(double noiseTolerance) {
            this.noiseTolerance = noiseTolerance;
            return this;
        }

        public Router build() {
            return new Router(this.bufferSize, this.noiseTolerance);
        }


    }


}
