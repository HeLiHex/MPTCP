package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.RouteEvent;

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
        events.add(new TCPSendEvent(this.tcp));
        for (int i = 0; i < this.numberOfPacketsAcked; i++) {
            Channel channelUsed = this.tcp.getChannel();
            events.add(new ChannelEvent(channelUsed));
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
