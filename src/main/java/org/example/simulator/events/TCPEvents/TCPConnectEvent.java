package org.example.simulator.events.TCPEvents;

import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;

import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.run.RunNetworkNodeEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;

public class TCPConnectEvent extends Event {

    private final TCP client;
    private final Endpoint host;

    public TCPConnectEvent(Instant instant, TCP client, Endpoint host) {
        super(instant);
        this.client = client;
        this.host = host;
    }

    public TCPConnectEvent(TCP client, Endpoint host){
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

        Channel channel = ((Routable)this.client).getPath(this.host);
        events.add(new ChannelEvent(channel));
        events.add(new TCPConnectEvent(Instant.now().plus(Duration.ofMillis(1000)),this.client, this.host));
    }

}
