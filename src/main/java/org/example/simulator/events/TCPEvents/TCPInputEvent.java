package org.example.simulator.events.TCPEvents;

import org.example.network.Channel;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;

public class TCPInputEvent extends Event {

    private final TCP tcp;
    private int numberOfPacketsAcked;

    public TCPInputEvent(TCP tcp) {
        super();
        if (tcp == null) throw new IllegalArgumentException("given TCP can not be null");
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.numberOfPacketsAcked = ((AbstractTCP)this.tcp).handleIncoming();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        for (int i = 0; i < this.numberOfPacketsAcked; i++) {
            Channel channelUsed = this.tcp.getChannel();
            events.add(new ChannelEvent(channelUsed));
        }
        events.add(new TCPSendEvent(this.tcp));
    }

}
