package org.example.simulator.events;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;

import java.time.Instant;
import java.util.Queue;

public class RunNetworkNodeEvent extends Event{

    private NetworkNode node;
    private Packet packet;

    public RunNetworkNodeEvent(Instant instant, NetworkNode node) {
        super(instant);
        this.node = node;
    }

    public RunNetworkNodeEvent(NetworkNode node) {
        super();
        this.node = node;
    }

    @Override
    public void run() {
        this.packet = this.node.peekInputBuffer();
        this.node.run();

/*

        Packet packet = this.node.peekInputBuffer();
        NetworkNode destination;
        if (packet == null){
            if (node instanceof TCP) destination = ((TCP)node).getConnectedEndpoint();
            else return;
        }else{
            destination = packet.getDestination();
        }
        this.path = this.node.getPath(destination);
        this.node.run();

 */
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.packet == null){
            System.out.println("packet is null");
            return;
        }

        NetworkNode nextNode = this.node.getPath(this.packet.getDestination()).getDestination();
        if (nextNode instanceof Endpoint){
            events.add(new RunEndpointEvent((Endpoint) nextNode));
            return;
        }

        if (nextNode instanceof Endpoint){
            events.add(new RunEndpointEvent((Endpoint) nextNode));
            return;
        }
        events.add(new RunNetworkNodeEvent(nextNode));
    }
}
