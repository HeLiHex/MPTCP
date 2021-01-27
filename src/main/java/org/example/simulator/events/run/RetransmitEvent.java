package org.example.simulator.events.run;

import org.example.data.Packet;
import org.example.protocol.BasicTCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.EventGenerator;
import org.example.simulator.events.RouteEvent;

import java.time.Instant;
import java.util.Queue;

public class RetransmitEvent extends EventGenerator {

    private final BasicTCP tcp;
    private final Packet packet;

    public RetransmitEvent(BasicTCP tcp, Packet packet) {
        super(Instant.now().plus(tcp.getTimeoutDuration()));
        this.tcp = tcp;
        this.packet = packet;
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (!this.tcp.isConnected()) return;

        if (this.tcp.inSendingWindow(this.packet)){
            events.add(new RouteEvent(this.tcp, packet));
            events.add(new RetransmitEvent(this.tcp, packet));
        }
    }


}
