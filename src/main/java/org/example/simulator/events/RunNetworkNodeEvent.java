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
    private Channel path;

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
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.path == null) return;
        NetworkNode nextNode = this.path.getDestination();
        events.add(new RunNetworkNodeEvent(nextNode));
    }
}
