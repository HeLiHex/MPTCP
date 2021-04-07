package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.protocol.TCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;
import org.example.simulator.events.EventGenerator;
import org.example.simulator.events.RouteEvent;

import java.util.Queue;

public class TCPRetransmitEventGenerator extends EventGenerator {

    private final TCP tcp;
    private final Packet packet;
    private final int numAttempts;

    public TCPRetransmitEventGenerator(Packet packe, int numAttempts) {
        super(((TCP) packe.getOrigin()).getRTO());
        this.tcp = (TCP) packe.getOrigin();
        this.packet = packe;
        this.numAttempts = numAttempts;
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.numAttempts > 3 ) return;
        if (!this.tcp.isConnected()) return;

        if (this.tcp.canRetransmit(this.packet)) {
            //Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, () -> "Retransmit packet: " + this.packet);
            Statistics.packetRetransmit();
            events.add(new RouteEvent(this.tcp, this.packet));
            events.add(new TCPRetransmitEventGenerator(this.packet, this.numAttempts + 1 ));
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
