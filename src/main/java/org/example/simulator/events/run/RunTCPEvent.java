package org.example.simulator.events.run;

import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;

public class RunTCPEvent extends RunEndpointEvent {


    public RunTCPEvent(Instant instant, Endpoint node) {
        super(instant, node);
    }

    public RunTCPEvent(Endpoint client) {
        super(client);
    }

    @Override
    public void generateSelf(Queue<Event> events) {
        BasicTCP tcp = (BasicTCP) this.node;
        if (tcp.isConnected()){
            boolean hasWaitingPackets = tcp.hasWaitingPackets();
            boolean hasPacketToSend = !tcp.outputBufferIsEmpty() && tcp.isWaitingForACK();

            boolean shouldRunAgain = hasPacketToSend || hasWaitingPackets;
            if (shouldRunAgain){
                //System.out.println("hasWaitingPackets: " + hasWaitingPackets);
                //System.out.println("hasPacketToSend: " + hasPacketToSend);
                //System.out.println("hasPacketToProcess: " + hasPacketsToProcess);
                events.add(new RunTCPEvent(Instant.now().plus(Duration.ofMillis(10)), (Endpoint) this.node));
            }
        }

    }
}
