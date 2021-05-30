package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.protocol.ClassicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.EventGenerator;
import org.example.simulator.events.RouteEvent;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPRetransmitEventGenerator extends EventGenerator {

    private final TCP tcp;
    private final Packet packet;
    private final int numAttempts;

    public TCPRetransmitEventGenerator(Packet packet, int numAttempts) {
        super(((TCP) packet.getOrigin()).getRTO());
        this.tcp = (TCP) packet.getOrigin();
        this.packet = packet;
        this.numAttempts = numAttempts;
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.numAttempts > 3) return;
        if (!this.tcp.isConnected()) return;
        if (this.tcp.canRetransmit(this.packet)) {
            ((ClassicTCP) this.tcp).getStats().packetRetransmit();
            events.add(new RouteEvent(this.tcp, this.packet));
            events.add(new TCPRetransmitEventGenerator(this.packet, this.numAttempts + 1));
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
