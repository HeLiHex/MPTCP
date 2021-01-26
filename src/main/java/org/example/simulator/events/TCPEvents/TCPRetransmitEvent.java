package org.example.simulator.events.TCPEvents;

import org.example.data.Packet;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;

public class TCPRetransmitEvent extends Event {

    private final TCP tcp;

    public TCPRetransmitEvent(Instant instant, TCP tcp) {
        super(instant);
        if (tcp == null) throw new IllegalArgumentException("given TCP can not be null");
        this.tcp = tcp;
    }

    public TCPRetransmitEvent(TCP tcp) {
        if (tcp == null) throw new IllegalArgumentException("given TCP can not be null");
        this.tcp = tcp;
    }

    @Override
    public void run() {
        AbstractTCP tmpTCP = ((AbstractTCP)this.tcp);
        Packet[] packets = tmpTCP.packetsToRetransmit();
        for (Packet packet : packets) {
            tmpTCP.route(packet);
        }
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        //todo - add new Event
    }
}
