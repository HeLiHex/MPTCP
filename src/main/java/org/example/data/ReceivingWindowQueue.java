package org.example.data;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class ReceivingWindowQueue extends PriorityBlockingQueue<Packet> {

    private int size;
    private Queue<Packet> received;

    public ReceivingWindowQueue(int capacity) {
        super(capacity, (packet, t1) -> packet.getAcknowledgmentNumber() - t1.getAcknowledgmentNumber());
        this.received = new ArrayDeque();
        this.size = capacity;
    }

    public void addToReceived(){
        //todo - implementasjons detalj
        //denne må endres etter implementasjon
        this.received.offer(this.poll());
    }

    public boolean readyToReceive(){
        return !received.isEmpty();
    }

    public Packet receive(){
        return received.poll();
    }

    public int getSize() {
        return this.size;
    }
}
