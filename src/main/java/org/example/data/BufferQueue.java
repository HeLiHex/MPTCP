package org.example.data;

import java.util.concurrent.ArrayBlockingQueue;

public class BufferQueue<T> extends ArrayBlockingQueue<T> {

    public BufferQueue(int capacity) {
        super(capacity);
    }
}
