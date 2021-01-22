package org.example.simulator.events.run;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.NetworkNode;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;

import java.time.Instant;
import java.util.Queue;

public class RunNetworkNodeEvent extends RunEvent {


    public RunNetworkNodeEvent(Instant instant, NetworkNode node) {
        super(instant, node);
    }

    public RunNetworkNodeEvent(NetworkNode node) {
        super(node);
    }

    @Override
    public void generateSelf(Queue<Event> events) {
        if (this.node.peekInputBuffer() != null){
            events.add(new RunNetworkNodeEvent(this.node));
        }
    }

    @Override
    public void setNextNode() {
        Packet packet = this.node.peekInputBuffer();
        if (packet == null) return;

        NetworkNode destination = packet.getDestination();
        Channel channel = this.node.getPath(destination);
        this.nextNode = channel.getDestination();
    }

    @Override
    public void updateStatistics(Statistics statistics) {

    }
}