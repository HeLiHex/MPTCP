package org.example.network;

import org.example.protocol.util.Packet;

public class Router extends Routable {

    public Router() {
        super();
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
    public boolean inputQueueIsEmpty() {
        return this.inputBuffer.isEmpty();
    }

    @Override
    public Packet dequeueOutputBuffer() {
        return this.dequeueInputBuffer();
    }

    @Override
    public boolean enqueueOutputBuffer(Packet packet) {
        return this.enqueueInputBuffer(packet);
    }

    @Override
    public boolean outputQueueIsEmpty() {
        return this.inputQueueIsEmpty();
    }

    @Override
    public void run() {
        while (true){
            if (!this.inputQueueIsEmpty()){
                this.route(this.dequeueInputBuffer());
            }
        }
    }



}
