package org.example.simulator.events.TCPevents;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.Event;
import org.example.simulator.events.run.RunNetworkNodeEvent;

import java.time.Instant;
import java.util.Queue;

public class TrySendEvent extends Event {

    private final AbstractTCP tcp;
    private boolean packetSent;

    public TrySendEvent(Instant instant, AbstractTCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public TrySendEvent(AbstractTCP tcp) {
        this.tcp = tcp;
    }


    @Override
    public void run() {
        this.packetSent = this.tcp.trySend();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.packetSent){
            events.add(new TrySendEvent(this.tcp));
            return;
        }
        Endpoint destination = this.tcp.getConnectedEndpoint();
        Channel channel = this.tcp.getPath(destination);
        NetworkNode nextNode = channel.getDestination();
        events.add(new RunNetworkNodeEvent(nextNode));
    }

    @Override
    public void updateStatistics(Statistics statistics) {
         if (this.packetSent) statistics.packetSent();
    }
}
