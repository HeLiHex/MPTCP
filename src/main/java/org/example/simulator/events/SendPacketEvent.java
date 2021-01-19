package org.example.simulator.events;

import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;

import java.time.Instant;
import java.util.Queue;

public class SendPacketEvent extends Event{

    private final TCP tcp;
    private final Packet packet;

    public SendPacketEvent(Instant instant, TCP tcp, Packet packet) {
        super(instant);
        this.tcp = tcp;
        this.packet = packet;
    }

    public SendPacketEvent(TCP tcp, Packet packet) {
        this.tcp = tcp;
        this.packet = packet;
    }

    @Override
    public void run() {
        this.tcp.send(this.packet);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        events.add(new RunNetworkNodeEvent((NetworkNode)this.tcp));
    }
}
