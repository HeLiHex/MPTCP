package org.example.simulator.events;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.MPTCP;

import java.util.List;
import java.util.Queue;

public class RouteEvent extends Event {

    private final Packet packet;
    private final Endpoint endpoint;

    public RouteEvent(Endpoint endpoint, Packet packet) {
        super();
        if (endpoint instanceof MPTCP) throw new IllegalArgumentException("Should only handle Regular tcp or subflows");
        this.endpoint = endpoint;
        this.packet = packet;
    }

    @Override
    public void run() {
        this.endpoint.route(this.packet);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        List<Channel> channelsUsed = this.endpoint.getChannelsUsed();
        Channel channel = channelsUsed.get(0);
        if (channel == null) return;
        events.add(new ChannelEvent(channel));
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
