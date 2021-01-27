package org.example.simulator.events.run;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.RouteEvent;
import org.example.simulator.events.TCPEvents.TCPInputEvent;
import org.example.simulator.events.TCPEvents.TCPSendEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
/*
public class RunTCPEvent extends Event {


    private final TCP tcp;
    private NetworkNode nextNode;

    public RunTCPEvent(Instant instant, TCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public RunTCPEvent(TCP tcp) {
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.setNextNode();
        //((Endpoint)this.tcp).run();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        this.generateSelf(events);
        if (this.nextNode == null) return;

        if (this.nextNode instanceof TCP){
            events.add(new RunTCPEvent((TCP) this.nextNode));
            return;
        }
        if (this.nextNode instanceof Endpoint){
            events.add(new RunEndpointEvent((Endpoint) this.nextNode));
            return;
        }
        events.add(new RunNetworkNodeEvent(this.nextNode));
    }

    private void setNextNode(){
        if (tcp.isConnected()){
            Channel channelOnPath = ((Endpoint)this.tcp).getPath(this.tcp.getConnectedEndpoint());
            this.nextNode = channelOnPath.getDestination();
            return;
        }

        Packet packet = ((Endpoint)this.tcp).peekInputBuffer();
        if (packet == null) return;

        Channel channelOnPath = ((Endpoint)this.tcp).getPath(packet.getDestination());
        this.nextNode = channelOnPath.getDestination();
    }

    private void generateSelf(Queue<Event> events) {
        BasicTCP tcp = (BasicTCP) this.tcp;
        if (tcp.isConnected()){
            boolean hasWaitingPackets = tcp.hasWaitingPackets();
            boolean hasPacketToSend = !tcp.outputBufferIsEmpty() && tcp.isWaitingForACK();

            boolean shouldRunAgain = hasPacketToSend || hasWaitingPackets;
            if (shouldRunAgain){
                events.add(new RunTCPEvent(Instant.now().plus(Duration.ofMillis(1000)), this.tcp));
            }
        }
    }




}

 */
