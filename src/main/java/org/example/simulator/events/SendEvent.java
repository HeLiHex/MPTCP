package org.example.simulator.events;

import org.example.data.Payload;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;

import java.util.Queue;

public class SendEvent extends Event{

    private TCP tcp;
    private Payload payload;
    private boolean notConnected = false;

    public SendEvent(double time, TCP tcp, Payload payload) {
        super(time);
        this.tcp = tcp;
        this.payload = payload;
    }

    @Override
    public void run() {
        this.tcp.send(payload);
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        //todo - add next event

    }
}
