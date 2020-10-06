package org.example.data;

import java.util.concurrent.ArrayBlockingQueue;

public class BufferQueue<Packet> extends ArrayBlockingQueue<Packet> {

    public BufferQueue(int capacity) {
        super(capacity);
    }
}
