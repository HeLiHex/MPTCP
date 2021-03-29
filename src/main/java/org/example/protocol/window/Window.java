package org.example.protocol.window;

import org.example.data.Packet;
import org.example.util.BoundedPriorityBlockingQueue;

import java.util.Comparator;

public abstract class Window extends BoundedPriorityBlockingQueue<Packet> implements IWindow {

    protected Window(int windowCapacity, Comparator<Packet> comparator) {
        super(windowCapacity, comparator);
    }

    @Override
    public boolean inWindow(int packetIndex) {
        return packetIndex < this.getWindowCapacity() && packetIndex >= 0;
    }

    @Override
    public int getWindowCapacity() {
        return this.bound();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Packet packet : this) {
            stringBuilder.append("[");
            stringBuilder.append(packet);
            stringBuilder.append("]");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
