package org.example.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BoundedPriorityBlockingQueue<T> implements BoundedQueue<T> {


    private PriorityBlockingQueue<T> pbq;
    private int bound;

    public BoundedPriorityBlockingQueue(int bound, Comparator<T> comparator) {
        this.pbq = new PriorityBlockingQueue<>(bound, comparator);
        this.bound = bound;
    }

    public BoundedPriorityBlockingQueue(int bound) {
        this.pbq = new PriorityBlockingQueue<>(bound);
        this.bound = bound;
    }

    @Override
    public boolean isFull(){
        if (this.pbq.size() > this.bound) throw new IllegalStateException("The queue contains more elements than it can take");
        return this.pbq.size() == this.bound;
    }

    @Override
    public boolean offer(T t) {
        if (isFull()) return false;
        return this.pbq.offer(t);
    }

    @Override
    public void put(T t) throws InterruptedException {
        if (isEmpty()) return;
        this.pbq.put(t);
    }

    @Override
    public boolean offer(T t, long l, TimeUnit timeUnit) throws InterruptedException {
        if (isFull()) return false;
        return this.pbq.offer(t);
    }

    @Override
    public T take() throws InterruptedException {
        return this.pbq.take();
    }

    @Override
    public T poll(long l, TimeUnit timeUnit) throws InterruptedException {
        return this.pbq.poll();
    }

    @Override
    public int remainingCapacity() {
        return this.bound - this.pbq.size();
    }


    @Override
    public int bound() {
        return this.bound;
    }

    @Override
    public void setBound(int bound) {
        this.bound = bound;
    }

    /**
     * @deprecated because it may violate the bound constraint of this class.
     * New implementation that follows the capacity constraint is needed
     */
    @Override
    @Deprecated(since = "4.5", forRemoval = true)
    public int drainTo(Collection<? super T> collection) {
        return this.pbq.drainTo(collection);
    }

    /**
     * @deprecated because it may violate the bound constraint of this class.
     * New implementation that follows the capacity constraint is needed
     */
    @Override
    @Deprecated(since = "4.5", forRemoval = true)
    public int drainTo(Collection<? super T> collection, int i) {
        return this.pbq.drainTo(collection, i);
    }

    @Override
    public T remove() {
        return this.pbq.remove();
    }

    @Override
    public T poll() {
        return this.pbq.poll();
    }

    @Override
    public T element() {
        return this.pbq.element();
    }

    @Override
    public T peek() {
        return this.pbq.peek();
    }

    @Override
    public int size() {
        return this.pbq.size();
    }

    @Override
    public boolean isEmpty() {
        return this.pbq.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.pbq.contains(o);
    }

    @Override
    public Object[] toArray() {
        return this.pbq.toArray();
    }

    @Override
    public <E> E[] toArray(E[] ts) {
        return this.pbq.toArray(ts);
    }

    @Override
    public boolean add(T t) {
        if (isFull()) return false;
        return this.pbq.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return this.pbq.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.pbq.containsAll(collection);
    }

    /**
     * @deprecated because it may violate the bound constraint of this class.
     * New implementation that follows the capacity constraint is needed
     */
    @Override
    @Deprecated(since = "4.5", forRemoval = true)
    public boolean addAll(Collection<? extends T> collection) {
        return this.pbq.addAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.pbq.removeAll(collection);
    }

    /**
     * @deprecated because it may violate the bound constraint of this class.
     * New implementation that follows the capacity constraint is needed
     */
    @Override
    @Deprecated(since = "4.5", forRemoval = true)
    public boolean retainAll(Collection<?> collection) {
        return this.pbq.retainAll(collection);
    }

    @Override
    public void clear() {
        this.pbq.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return this.pbq.iterator();
    }
}
