package org.example.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public interface BoundedQueue<T> extends Serializable, Iterable<T>, Collection<T>, BlockingQueue<T>, Queue<T> {

    boolean isFull();
}
