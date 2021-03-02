package org.example.simulator.events.tcp;

import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.Queue;

public class TCPConnectEvent extends Event {

    private final TCP client;
    private final Endpoint host;

    public TCPConnectEvent(int delay, TCP client, Endpoint host) {
        super(delay);
        this.client = client;
        this.host = host;
    }

    public TCPConnectEvent(TCP client, Endpoint host) {
        super();
        this.client = client;
        this.host = host;
    }

    @Override
    public void run() {
        if (this.client.isConnected()) return;
        this.client.connect(this.host);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.client.isConnected()) return;

        Channel channel = ((Routable) this.client).getPath(this.host);
        events.add(new ChannelEvent(channel));
        events.add(new TCPConnectEvent(1000, this.client, this.host));
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
