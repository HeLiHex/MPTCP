package org.example.network;

import org.example.data.BufferQueue;
import java.util.Random;

public class Router extends Routable {

    public static class RouterBuilder{

        private int bufferSize = 10;
        private Random random = new Random(1337);
        private double noiseTolerance = 100.0;

        public RouterBuilder withBufferSize(int bufferSize){
            this.bufferSize = bufferSize;
            return this;
        }

        public RouterBuilder withRandomGenerator(Random randomGenerator){
            this.random = randomGenerator;
            return this;
        }

        public RouterBuilder withNoiseTolerance(double noiseTolerance){
            this.noiseTolerance = noiseTolerance;
            return this;
        }

        public Router build(){
            return new Router(this.bufferSize, this.random, this.noiseTolerance);
        }


    }

    private Router(int bufferSize, Random randomGenerator, double noiseTolerance) {
        super(new BufferQueue<>(bufferSize), randomGenerator, noiseTolerance);
    }

    @Override
    public void run() {
        while (true){
            if (!this.inputBufferIsEmpty()){
                this.route(this.dequeueInputBuffer());
            }
        }
    }



}
