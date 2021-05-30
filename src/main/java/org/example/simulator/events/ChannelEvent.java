package org.example.simulator.events;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;
import org.example.simulator.events.run.RunEndpointEvent;
import org.example.simulator.events.run.RunNetworkNodeEvent;
import org.example.simulator.events.tcp.RunTCPEvent;

import java.util.Queue;

public class ChannelEvent extends Event {

    private final Channel channel;
    private boolean channelSuccess;

    public ChannelEvent(Channel channel) {
        super(channel);
        this.channel = channel;
        this.channelSuccess = false;
    }

    @Override
    public void run() {
        this.channelSuccess = this.channel.channel();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.channelSuccess) {
            NetworkNode nextNode = this.channel.getDestination();
            if (nextNode instanceof TCP) {
                var tcp = ((TCP) nextNode).getMainFlow();
                events.add(new RunTCPEvent(tcp));
                return;
            }
            assert !nextNode.inputBufferIsEmpty() : "The next NetworkNode has no packet in the input buffer";
            if (nextNode instanceof Endpoint) {
                events.add(new RunEndpointEvent((Endpoint) nextNode));
                return;
            }
            events.add(new RunNetworkNodeEvent(nextNode));
            return;
        }

        NetworkNode nextNode = this.channel.getDestination();
        if (nextNode instanceof TCP) {
            var tcp = ((TCP) nextNode).getMainFlow();
            if (!tcp.inputBufferIsEmpty()) return;
            events.add(new RunTCPEvent(tcp));
        }


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
