package org.example.simulator.events;

import org.example.data.Payload;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.run.RunTCPEvent;


import java.time.Instant;
import java.util.Queue;

public class SendEvent extends Event{

    private final TCP tcp;
    private final Payload payload;

    public SendEvent(Instant instant, TCP tcp, Payload payload) {
        super(instant);
        this.tcp = tcp;
        this.payload = payload;
    }

    public SendEvent(TCP tcp, Payload payload) {
        super();
        this.tcp = tcp;
        this.payload = payload;
    }

    @Override
    public void run() {
        this.tcp.send(this.payload);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        events.add(new RunTCPEvent(this.tcp));
    }

}
