package org.example.simulator.events.run;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.TCPEvents.TCPInputEvent;

import java.time.Instant;
import java.util.Queue;

public class RunNetworkNodeEvent extends Event {

    private final NetworkNode node;
    private Endpoint packetDestination;

    public RunNetworkNodeEvent(Instant instant, NetworkNode node) {
        super(instant);
        this.node = node;
        if (this.node == null) throw new IllegalStateException("wtf");
    }

    public RunNetworkNodeEvent(NetworkNode node) {
        super();
        this.node = node;
        if (this.node == null) throw new IllegalStateException("wtf");
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
