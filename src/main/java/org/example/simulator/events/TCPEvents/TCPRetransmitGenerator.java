package org.example.simulator.events.TCPEvents;

import org.example.data.Packet;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.EventGenerator;
import org.example.simulator.events.RouteEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
/*
public class TCPRetransmitGenerator extends EventGenerator {

    private final TCP tcp;

    public TCPRetransmitGenerator(Instant instant, TCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public TCPRetransmitGenerator(TCP tcp) {
        this.tcp = tcp;
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        AbstractTCP tmpTCP = ((AbstractTCP)this.tcp);
        Packet[] packets = tmpTCP.packetsToRetransmit();
        for (Packet packet : packets) {
            events.add(new RouteEvent(tmpTCP, packet));
        }
        events.add(new TCPSendEvent(Instant.now().plus(Duration.ofMillis(1)),this.tcp));
    }
}

 */
