package org.example.simulator.events.TCPEvents;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;

public class TCPSendEvent extends Event {

    private final TCP tcp;
    private Packet packetSent;


    public TCPSendEvent(Instant instant, TCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public TCPSendEvent(TCP tcp) {
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.packetSent = ((AbstractTCP)this.tcp).trySend();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.packetSent != null) {
            if (tcp.isConnected()) {
                events.add(new TCPSendEvent(this.tcp));
                events.add(new TCPRetransmitEventGenerator((BasicTCP) this.tcp, this.packetSent));
            }
            Channel channel = this.tcp.getChannel();
            events.add(new ChannelEvent(channel));
        }
    }

}

