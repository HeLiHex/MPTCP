package org.example.simulator.events.TCPEvents;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.run.RunEndpointEvent;
import org.example.simulator.events.run.RunNetworkNodeEvent;

import java.time.Instant;
import java.util.Queue;

public class TCPSendEvent extends Event {

    private final TCP tcp;
    private Packet packetSent;


    public TCPSendEvent(Instant instant, TCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public TCPSendEvent(TCP tcp) {
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.packetSent = ((AbstractTCP)this.tcp).trySend();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (tcp.isConnected()){
            if (this.packetSent != null){
                events.add(new TCPSendEvent(this.tcp));
                events.add(new TCPRetransmitEventGenerator((BasicTCP)this.tcp, this.packetSent));
            }
            Channel channel = ((Endpoint)this.tcp).getPath(this.tcp.getConnectedEndpoint());
            events.add(new ChannelEvent(channel));
            return;
        }
        Channel channel = ((Endpoint)this.tcp).getChannels().get(0); //todo - what happens here if we have MPTCP
        events.add(new ChannelEvent(channel));
    }

    private NetworkNode findNextNode(){
        AbstractTCP tmpTCP = ((AbstractTCP)this.tcp);
        if (tmpTCP.isConnected()){
            return tmpTCP.getPath(tmpTCP.getConnectedEndpoint()).getDestination();
        }

        if (!tmpTCP.outputBufferIsEmpty()){
            Channel channel = tmpTCP.getChannels().get(0); // TODO - this may be a problem when implementing MPTCP
            if (channel == null) throw new IllegalStateException("There are no channels");
            return channel.getDestination();
        }

        return null;
    }
}

