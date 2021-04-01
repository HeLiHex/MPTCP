package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.protocol.TCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.RouteEvent;

import java.util.List;
import java.util.Queue;

public class TCPInputEvent extends Event {

    private final TCP tcp;

    public TCPInputEvent(TCP tcp) {
        super(tcp);
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.tcp.handleIncoming();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        List<Channel> channelsUsed = this.tcp.getChannelsUsed();
        for (Channel channel : channelsUsed) {
            events.add(new ChannelEvent(channel));
        }
        events.add(new TCPSendEvent(this.tcp));
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
