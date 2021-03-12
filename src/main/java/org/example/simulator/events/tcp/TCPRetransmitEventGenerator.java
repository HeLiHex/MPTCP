package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.protocol.ClassicTCP;
import org.example.protocol.TCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;
import org.example.simulator.events.EventGenerator;
import org.example.simulator.events.RouteEvent;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPRetransmitEventGenerator extends EventGenerator {

    private final TCP tcp;
    private final Packet packet;

    public TCPRetransmitEventGenerator(TCP tcp, Packet packet) {
        super(tcp.getRTO());
        this.tcp = tcp;
        this.packet = packet;
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (!this.tcp.isConnected()) return;

        if (this.tcp.canRetransmit(packet)) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, () -> "Retransmit packet: " + this.packet);
            Statistics.packetRetransmit();
            events.add(new RouteEvent(this.tcp, packet));
            events.add(new TCPRetransmitEventGenerator(this.tcp, packet));
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
