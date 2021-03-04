package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.Queue;

public class TCPSendEvent extends Event {

    private final TCP tcp;
    private Packet packetSent;


    public TCPSendEvent(TCP tcp) {
        super(((BasicTCP) tcp).processingDelay());
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.packetSent = ((BasicTCP) this.tcp).trySend();
        if (this.packetSent != null) Statistics.packetSent();
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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}

