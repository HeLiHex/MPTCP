package org.example.simulator.events.tcp;

import org.example.network.Channel;
import org.example.protocol.ClassicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.Queue;

public class TCPInputEvent extends Event {

    private final TCP tcp;
    private boolean ackSent;

    public TCPInputEvent(TCP tcp) {
        super(((ClassicTCP) tcp).processingDelay());
        if (tcp == null) throw new IllegalArgumentException("given TCP can not be null");
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.ackSent = ((ClassicTCP) this.tcp).handleIncoming();

    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.ackSent) {
            Channel channelUsed = this.tcp.getChannel();
            events.add(new ChannelEvent(channelUsed));
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
