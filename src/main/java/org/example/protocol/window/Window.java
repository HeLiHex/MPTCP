package org.example.protocol.window;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.Comparator;

public abstract class Window extends BoundedPriorityBlockingQueue<Packet> implements IWindow {

    protected final Connection connection;

    public Window(int windowSize, Connection connection, Comparator comparator) {
        super(windowSize, comparator);
        this.connection = connection;
    }

    @Override
    public boolean inWindow(int packetIndex){
        return packetIndex < this.getWindowCapacity() && packetIndex >= 0;
    }

    @Override
    public int getWindowCapacity() {
        return this.bound();
    }
}
