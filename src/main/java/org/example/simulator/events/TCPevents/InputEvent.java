package org.example.simulator.events.TCPevents;

import org.example.protocol.AbstractTCP;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;

public class InputEvent extends Event {

    private final AbstractTCP tcp;

    public InputEvent(Instant instant, AbstractTCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public InputEvent(AbstractTCP tcp) {
        this.tcp = tcp;
    }


    @Override
    public void run() {
        this.tcp.handleIncoming();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        events.add(new RetransmitEvent(this.tcp));
        //todo this creates a loop
        //if (this.tcp.inputBufferIsEmpty()) events.add(new RetransmitEvent(this.tcp));
        //else events.add(new InputEvent(this.tcp));
    }
}
