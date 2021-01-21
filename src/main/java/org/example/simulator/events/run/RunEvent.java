package org.example.simulator.events.run;

import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;

public abstract class RunEvent extends Event {

    protected final NetworkNode node;
    protected NetworkNode nextNode;


    public RunEvent(Instant instant, NetworkNode node) {
        super(instant);
        this.node = node;
    }

    public RunEvent(NetworkNode node) {
        super();
        this.node = node;
    }


    public abstract void setNextNode();

    @Override
    public void run(){
        this.setNextNode();
        this.node.run();
    }


    public abstract void generateSelf(Queue<Event> events);


    @Override
    public void generateNextEvent(Queue<Event> events) {
        this.generateSelf(events);
        if (this.nextNode == null) return;

        if (this.nextNode instanceof TCP){
            events.add(new RunTCPEvent((AbstractTCP) this.nextNode));
            return;
        }
        if (this.nextNode instanceof Endpoint){
            events.add(new RunEndpointEvent((Endpoint) this.nextNode));
            return;
        }
        events.add(new RunNetworkNodeEvent(this. nextNode));
    }
}
