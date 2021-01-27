package org.example.simulator.events.run;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.TCPEvents.TCPInputEvent;

import java.time.Instant;
import java.util.Queue;

public class RunNetworkNodeEvent extends Event {

    private final NetworkNode node;
    private NetworkNode nextNode;

    public RunNetworkNodeEvent(Instant instant, NetworkNode node) {
        super(instant);
        this.node = node;
        if (this.node == null) throw new IllegalStateException("wtf");
    }

    public RunNetworkNodeEvent(NetworkNode node) {
        super();
        this.node = node;
        if (this.node == null) throw new IllegalStateException("wtf");
    }

    @Override
    public void run() {
        this.setNextNode();
        this.node.run();
    }

    private void setNextNode() {
        Packet packet = this.node.peekInputBuffer();
        if (packet == null) return;

        Channel channelOnPath = this.node.getPath(packet.getDestination());
        this.nextNode = channelOnPath.getDestination();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (!this.node.inputBufferIsEmpty()){
            events.add(new RunNetworkNodeEvent(this.node));
            return;
        }

        if (this.nextNode == null){
            return;
        }

        if (this.nextNode instanceof TCP) {
            events.add(new TCPInputEvent((TCP) this.nextNode));
            return;
        }
        if (this.nextNode instanceof Endpoint) {
            events.add(new RunEndpointEvent((Endpoint) this.nextNode));
            return;
        }
        events.add(new RunNetworkNodeEvent(this.nextNode));
    }

}
