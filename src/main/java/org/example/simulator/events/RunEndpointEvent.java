package org.example.simulator.events;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;

import java.time.Instant;
import java.util.Queue;

public class RunEndpointEvent extends Event {

    private final Endpoint client;
    private final Channel path;

    public RunEndpointEvent(Instant instant, Endpoint client) {
        super(instant);
        this.client = client;

        if (this.client.isConnected()){
            Endpoint endpoint = this.client.getConnectedEndpoint();
            this.path = this.client.getPath(endpoint);
        }else{
            this.path = this.client.getChannels().get(0);
        }
    }

    public RunEndpointEvent(Endpoint client) {
        super();
        this.client = client;

        if (this.client.isConnected()){
            Endpoint endpoint = this.client.getConnectedEndpoint();
            this.path = this.client.getPath(endpoint);
        }else{
            this.path = this.client.getChannels().get(0);
        }
    }

    @Override
    public void run() {
        this.client.run();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        NetworkNode nextNode = this.path.getDestination();
        if (nextNode instanceof TCP){
            events.add(new RunTCPEvent((AbstractTCP) nextNode));
            return;
        }
        if (nextNode instanceof Endpoint){
            events.add(new RunEndpointEvent((Endpoint) nextNode));
            return;
        }
        events.add(new RunNetworkNodeEvent(nextNode));
    }
}
