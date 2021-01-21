package org.example.simulator.events.TCPevents;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;
import org.example.simulator.events.run.RunNetworkNodeEvent;

import java.time.Instant;
import java.util.Queue;

public class ResendEvent extends Event {

    private final Packet packet;
    private final Endpoint endpoint;

    public ResendEvent(Instant instant, Endpoint endpoint, Packet packet) {
        super(instant);
        this.endpoint = endpoint;
        this.packet = packet;
    }

    public ResendEvent(Endpoint endpoint, Packet packet) {
        super();
        this.endpoint = endpoint;
        this.packet = packet;
    }

    @Override
    public void run() {
        this.endpoint.route(this.packet);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        Channel channel = this.endpoint.getPath(this.packet.getDestination());
        NetworkNode nextNode = channel.getDestination();
        events.add(new RunNetworkNodeEvent(nextNode));
    }

    @Override
    public void updateStatistics(Statistics statistics) {

    }
}
