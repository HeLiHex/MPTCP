package org.example.simulator.events.TCPevents;

import org.example.data.Packet;
import org.example.protocol.AbstractTCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;
import org.example.simulator.events.RouteEvent;

import java.time.Instant;
import java.util.Queue;

public class RetransmitEvent extends Event {

    private AbstractTCP tcp;

    public RetransmitEvent(Instant instant, AbstractTCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public RetransmitEvent(AbstractTCP tcp) {
        this.tcp = tcp;
    }

    @Override
    public void run() {
        //should not do more than
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
       Packet[] packets = this.tcp.packetsToRetransmit();
        for (Packet packet : packets){
            events.add(new ResendEvent(this.tcp, packet));
        }
        events.add(new TrySendEvent(this.tcp));
    }

    @Override
    public void updateStatistics(Statistics statistics) {

    }
}
