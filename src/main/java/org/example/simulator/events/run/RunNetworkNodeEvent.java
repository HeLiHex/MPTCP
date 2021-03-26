package org.example.simulator.events.run;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.List;
import java.util.Queue;

public class RunNetworkNodeEvent extends Event {

    private final NetworkNode node;
    private Endpoint packetDestination;

    public RunNetworkNodeEvent(NetworkNode node) {
        super(node);
        this.node = node;
    }

    @Override
    public void run() {
        this.packetDestination = this.node.peekInputBuffer().getDestination();
        this.node.run();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        List<Channel> channelsUsed = this.node.getChannelsUsed();
        assert channelsUsed.size() == 1 : "Multiple channels used in one NetworkNodeEvent";
        Channel channel = channelsUsed.get(0);
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
