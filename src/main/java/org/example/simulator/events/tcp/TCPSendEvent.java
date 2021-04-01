package org.example.simulator.events.tcp;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.protocol.TCP;
import org.example.simulator.Statistics;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;

import java.util.List;
import java.util.Queue;

public class TCPSendEvent extends Event {

    private final TCP tcp;
    private List<Packet> packetsSent;


    public TCPSendEvent(TCP tcp) {
        super(tcp.afterConnectSendDelay());
        this.tcp = tcp;
    }

    @Override
    public void run() {
        this.packetsSent = this.tcp.trySend();
        for (int i = 0; i < this.packetsSent.size(); i++) {
            Statistics.packetSent();
        }
    }

    @Override
    public void generateNextEvent(Queue<Event> events) {
        if (!this.packetsSent.isEmpty()) {
            if (this.tcp.isConnected()) {
                //events.add(new TCPSendEvent(this.tcp));
                for (Packet packet : this.packetsSent) {
                    events.add(new TCPRetransmitEventGenerator(this.tcp, packet));
                }
            }
            List<Channel> channelsUsed = this.tcp.getChannelsUsed();
            for (Channel channel : channelsUsed) {
                events.add(new ChannelEvent(channel));
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}

