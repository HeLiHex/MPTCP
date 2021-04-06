package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.List;
import java.util.Queue;

public class RunTCPEvent extends Event {

    private final TCP tcp;
    private List<Packet> packetsSent;
    private boolean scheduleFirstSend;

    public RunTCPEvent(TCP tcp) {
        super(tcp);
        this.tcp = tcp;
        this.scheduleFirstSend = false;
    }

    public RunTCPEvent(TCP tcp, long delay) {
        super(delay);
        this.tcp = tcp;
        this.scheduleFirstSend = false;
    }

    @Override
    public void run() {
        if (this.tcp.isConnected()){
            this.tcp.handleIncoming();
            this.packetsSent = this.tcp.trySend();
            return;
        }
        this.tcp.handleIncoming();
        this.scheduleFirstSend = true;
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        for (Channel channel : this.tcp.getChannelsUsed()) {
            events.add(new ChannelEvent(channel));
        }

        if (this.scheduleFirstSend){
            //add delay before sending first packet
            //this is important if we have a "downloading situation"
            events.add(new RunTCPEvent(this.tcp, 1000));
            return;
        }

        for (Packet packet : this.packetsSent) {
            events.add(new TCPRetransmitEventGenerator(packet, 0));
        }
    }

}
