package org.example.util;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public interface BoundedQueue<T> extends Iterable<T>, Collection<T>, BlockingQueue<T>, Queue<T> {

    boolean isFull();
}
