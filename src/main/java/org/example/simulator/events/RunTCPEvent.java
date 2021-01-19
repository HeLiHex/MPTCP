package org.example.simulator.events;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;

import java.time.Instant;
import java.util.Queue;

public class RunTCPEvent extends Event{

    private final AbstractTCP tcp;
    private final Channel path;

    public RunTCPEvent(Instant instant, AbstractTCP tcp) {
        super(instant);
        this.tcp = tcp;

        if (this.tcp.isConnected()){
            Endpoint endpoint = this.tcp.getConnectedEndpoint();
            this.path = this.tcp.getPath(endpoint);
        }else{
            this.path = this.tcp.getChannels().get(0);
        }

    }

    public RunTCPEvent(AbstractTCP tcp) {
        this.tcp = tcp;

        if (this.tcp.isConnected()){
            Endpoint endpoint = this.tcp.getConnectedEndpoint();
            this.path = this.tcp.getPath(endpoint);
        }else{
            this.path = this.tcp.getChannels().get(0);
        }
    }


    @Override
    public void run() {
        this.tcp.run();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        NetworkNode nextNode = this.path.getDestination();
        if (nextNode instanceof TCP){
            events.add(new RunTCPEvent((AbstractTCP) nextNode));
            return;
        }
        if (nextNode instanceof Endpoint){
            events.add(new RunEndpointEvent((Endpoint) nextNode));
            return;
        }
        events.add(new RunNetworkNodeEvent(nextNode));
    }
}
