package org.example.simulator.events;

import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;

import java.time.Instant;
import java.util.Queue;

public class ConnectEvent extends Event{

    private TCP client;
    private Endpoint host;
    private Channel path;

    public ConnectEvent(Instant instant, TCP client, Endpoint host) {
        super(instant);
        this.client = client;
        this.host = host;
    }

    public ConnectEvent(TCP client, Endpoint host){
        super();
        this.client = client;
        this.host = host;
    }

    @Override
    public void run() {
        this.path = ((Routable)this.client).getPath(this.host);
        this.client.connect(this.host);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        NetworkNode nextNode = path.getDestination();
        events.add(new RunNetworkNodeEvent(Instant.now(), nextNode));

    }
}
