package org.example.network;

import org.example.data.BufferQueue;
import org.example.data.Packet;

import java.util.Random;

public class Router extends Routable {

    public Router(int bufferSize, Random randomGenerator, double noiseTolerance) {
        super(new BufferQueue<Packet>(bufferSize), randomGenerator, noiseTolerance);
    }

    public Router(int bufferSize, Random randomGenerator) {
        super(new BufferQueue<Packet>(bufferSize), randomGenerator, 100.0);
    }



    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        return this.inputBuffer.offer(packet);
    }

    @Override
    public Packet dequeueInputBuffer() {
        return this.inputBuffer.poll();
    }

    @Override
    public boolean inputBufferIsEmpty() {
        return this.inputBuffer.isEmpty();
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
