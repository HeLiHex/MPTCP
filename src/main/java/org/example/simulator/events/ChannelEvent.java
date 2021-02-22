package org.example.simulator.events;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;
import org.example.simulator.events.TCPEvents.TCPInputEvent;
import org.example.simulator.events.run.RunEndpointEvent;
import org.example.simulator.events.run.RunNetworkNodeEvent;

import java.time.Instant;
import java.util.Queue;

public class ChannelEvent extends Event{

    private final Channel channel;
    private boolean channelSuccess;

    public ChannelEvent(Channel channel) {
        //super(Instant.now().plus(channel.propogationDelay()));
        this.channel = channel;
        this.channelSuccess = false;
    }

    @Override
    public void run() {
        this.channelSuccess = this.channel.channel();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.channelSuccess){
            NetworkNode nextNode = this.channel.getDestination();
            if (nextNode instanceof TCP) {
                events.add(new TCPInputEvent((TCP) nextNode));
                return;
            }
            if (nextNode instanceof Endpoint) {
                events.add(new RunEndpointEvent((Endpoint) nextNode));
                return;
            }
            events.add(new RunNetworkNodeEvent(nextNode));
        }
    }
}
