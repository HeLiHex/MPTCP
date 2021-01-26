package org.example.simulator.events.TCPEvents;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.AbstractTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.run.RunEndpointEvent;
import org.example.simulator.events.run.RunNetworkNodeEvent;

import java.time.Instant;
import java.util.Queue;
/*
public class TCPSendEvent extends Event {

    private final TCP tcp;
    private boolean sendSuccess;


    public TCPSendEvent(Instant instant, TCP tcp) {
        super(instant);
        this.tcp = tcp;
    }

    public TCPSendEvent(TCP tcp) {
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.sendSuccess = ((AbstractTCP)this.tcp).trySend();
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (this.sendSuccess){
            events.add(new TCPSendEvent(this.tcp));
            return;
        }

        NetworkNode nextNode = this.findNextNode();
        if (nextNode == null) return;

        if (nextNode instanceof TCP){
            events.add(new TCPInputEvent((TCP) nextNode));
            return;
        }
        if (nextNode instanceof Endpoint){
            events.add(new RunEndpointEvent((Endpoint) nextNode));
            return;
        }
        events.add(new RunNetworkNodeEvent(nextNode));
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

 */
