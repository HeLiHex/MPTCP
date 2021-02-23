package org.example.simulator.events.run;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.Queue;

public class RunNetworkNodeEvent extends Event {

    private final NetworkNode node;
    private Endpoint packetDestination;

    public RunNetworkNodeEvent(NetworkNode node) {
        super();
        this.node = node;
        if (this.node == null) throw new IllegalStateException("Node is null");
    }

    @Override
    public void run() {
        this.packetDestination = this.node.peekInputBuffer().getDestination();
        this.node.run();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        Channel channel = this.node.getPath(this.packetDestination);
        events.add(new ChannelEvent(channel));
    }

}
