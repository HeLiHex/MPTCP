package org.example.util;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public interface BoundedQueue<T> extends Iterable<T>, Collection<T>, BlockingQueue<T>, Queue<T> {

    /**
     * A method to check is the queue is full
     *
     * @return True if the queue is full.
     */
    boolean isFull();

    /**
     * A method that returns the bound of the BoundedQueue
     *
     * @return the bound
     */
    int bound();

    /**
     * A method to set the bound of the BoundedQueue
     *
     * @param bound
     */
    void setBound(int bound);
}
