package org.example.simulator.events.run;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;


public class RunEndpointEvent extends RunEvent {


    public RunEndpointEvent(Instant instant, Endpoint node) {
        super(instant, node);
    }

    public RunEndpointEvent(Endpoint client) {
        super(client);
    }

    @Override
    public void generateSelf(Queue<Event> events) {
        if (this.node.peekInputBuffer() != null){
            events.add(new RunEndpointEvent((Endpoint) this.node));
        }
    }

    @Override
    public void setNextNode() {
        final Endpoint endpoint = (Endpoint)this.node;
        if (endpoint.isConnected()){
            Endpoint destination = endpoint.getConnectedEndpoint();
            Channel channel = endpoint.getPath(destination);
            this.nextNode = channel.getDestination();
        }else{
            Channel channel = this.node.getChannels().get(0);
            this.nextNode = channel.getDestination();
        }
    }

    @Override
    public void updateStatistics(Statistics statistics) {

    }
}
