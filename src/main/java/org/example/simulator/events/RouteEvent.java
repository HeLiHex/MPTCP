package org.example.simulator.events;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.TCPEvents.TCPInputEvent;
import org.example.simulator.events.run.RunEndpointEvent;
import org.example.simulator.events.run.RunNetworkNodeEvent;

import java.time.Instant;
import java.util.Queue;

public class RouteEvent extends Event{

    private final Packet packet;
    private final Endpoint endpoint;

    public RouteEvent(Instant instant, Endpoint endpoint, Packet packet) {
        super(instant);
        this.endpoint = endpoint;
        this.packet = packet;
    }

    public RouteEvent(Endpoint endpoint, Packet packet) {
        super();
        this.endpoint = endpoint;
        this.packet = packet;
    }

    @Override
    public void run() {
        System.out.println("route!!!!");
        this.endpoint.route(this.packet);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        Channel channel = endpoint.getPath(packet.getDestination());
        if (channel == null) return;
        events.add(new ChannelEvent(channel));

    }

}
