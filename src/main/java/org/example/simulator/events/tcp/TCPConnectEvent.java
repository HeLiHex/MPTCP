package org.example.simulator.events.tcp;

import org.example.network.Channel;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.Queue;

public class TCPConnectEvent extends Event {

    private final TCP client;
    private final TCP host;
    private final int numAttempts;

    private TCPConnectEvent(int delay, TCP client, TCP host, int numAttempts) {
        super(delay);
        this.client = client;
        this.host = host;
        this.numAttempts = numAttempts;
    }

    public TCPConnectEvent(TCP client, TCP host) {
        super();
        this.client = client;
        this.host = host;
        this.numAttempts = 0;
    }

    @Override
    public void run() {
        if (this.client.isConnected()) return;
        this.client.connect(this.host);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.numAttempts > this.client.getNumberOfFlows() * 3) return;
        if (this.client.isConnected()) return;

        for (Channel channel : this.client.getChannelsUsed()) {
            events.add(new ChannelEvent(channel));
        }
        events.add(new TCPConnectEvent(100000, this.client, this.host, this.numAttempts + 1));
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
