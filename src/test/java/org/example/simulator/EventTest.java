package org.example.simulator;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.simulator.events.tcp.TCPRetransmitEventGenerator;
import org.example.simulator.events.tcp.TCPSendEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class EventTest {

    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    private Queue<Event> events;
    private AbstractTCP tcp;
    private AbstractTCP host;


    @Before
    public void setup(){
        this.events = new PriorityQueue<>();
        this.tcp = new BasicTCP();
        this.host = new BasicTCP();
    }

    public void connect(TCP linkedClient, Endpoint linkedServer){
        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(0, linkedClient, linkedServer));
        eventHandler.run();
    }

    @Test
    public void EventsOccurInAPreMatchedSequence(){
        this.tcp.addChannel(this.host);
        this.tcp.updateRoutingTable();
        this.host.updateRoutingTable();
        this.connect(this.tcp, this.host);

        this.tcp.send(new Message("hello"));

        this.events.add(new TCPSendEvent(this.tcp));

        Event curEvent = this.events.poll();
        Assert.assertEquals(TCPSendEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(TCPSendEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(ChannelEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(TCPInputEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(TCPSendEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(ChannelEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(TCPInputEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(TCPSendEvent.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        curEvent = this.events.poll();
        Assert.assertEquals(TCPRetransmitEventGenerator.class, curEvent.getClass());
        curEvent.run();
        curEvent.generateNextEvent(this.events);

        Assert.assertEquals(0, this.events.size());

    }




}
