package org.example.simulator.events.run;

import org.example.network.Channel;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.List;
import java.util.Queue;

public class RunNetworkNodeEvent extends Event {

    private final NetworkNode node;

    public RunNetworkNodeEvent(NetworkNode node) {
        super(node);
        this.node = node;
    }

    @Override
    public void run() {
        assert !this.node.inputBufferIsEmpty() : "RunNetworkNodeEvent added, but no packet to be sent";
        this.node.run();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        List<Channel> channelsUsed = this.node.getChannelsUsed();
        assert channelsUsed.size() == 1 : "Multiple channels used in one NetworkNodeEvent";
        var channel = channelsUsed.get(0);
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
