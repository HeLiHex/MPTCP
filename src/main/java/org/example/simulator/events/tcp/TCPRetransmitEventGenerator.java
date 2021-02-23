package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.protocol.BasicTCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;
import org.example.simulator.events.EventGenerator;
import org.example.simulator.events.RouteEvent;

import java.util.Queue;

public class TCPRetransmitEventGenerator extends EventGenerator {

    private final BasicTCP tcp;
    private final Packet packet;

    public TCPRetransmitEventGenerator(BasicTCP tcp, Packet packet) {
        super(1000);
        this.tcp = tcp;
        this.packet = packet;
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (!this.tcp.isConnected()) return;

        if (this.tcp.inSendingWindow(this.packet) && this.tcp.packetIsWaiting(this.packet)){
            Statistics.packetRetransmit();
            events.add(new RouteEvent(this.tcp, packet));
            events.add(new TCPRetransmitEventGenerator(this.tcp, packet));
        }
    }


}