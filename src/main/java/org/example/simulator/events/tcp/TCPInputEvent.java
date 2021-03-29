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
    private Packet packetToFastRetransmit;

    public TCPInputEvent(TCP tcp) {
        super(tcp);
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.tcp.handleIncoming();
        this.packetToFastRetransmit = this.tcp.fastRetransmit();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.packetToFastRetransmit != null) {
            events.add(new RouteEvent(this.tcp, this.packetToFastRetransmit));
            Statistics.packetFastRetransmit();
        }
        List<Channel> channelsUsed = this.tcp.getChannelsUsed();
        for (Channel channel : channelsUsed) {
            events.add(new ChannelEvent(channel));
        }
        /*
        if (this.ackSent) {
            Channel channelUsed = this.tcp.getChannel();
            events.add(new ChannelEvent(channelUsed));
        }
         */
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
