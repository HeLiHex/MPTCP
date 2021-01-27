package org.example.simulator.events.TCPEvents;

import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;

public class TCPInputEvent extends Event {

    private final TCP tcp;

    public TCPInputEvent(Instant instant, TCP tcp) {
        super(instant);
        if (tcp == null) throw new IllegalArgumentException("given TCP can not be null");
        this.tcp = tcp;
    }

    public TCPInputEvent(TCP tcp) {
        super();
        if (tcp == null) throw new IllegalArgumentException("given TCP can not be null");
        this.tcp = tcp;
    }

    @Override
    public void run() {
        ((AbstractTCP)this.tcp).handleIncoming();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        events.add(new TCPSendEvent(this.tcp));
    }

}
