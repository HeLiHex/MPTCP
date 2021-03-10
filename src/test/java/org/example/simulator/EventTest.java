package org.example.simulator;

import org.example.data.Message;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.ClassicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.simulator.events.tcp.TCPRetransmitEventGenerator;
import org.example.simulator.events.tcp.TCPSendEvent;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.PriorityQueue;
import java.util.Queue;

public class EventTest {

    private Queue<Event> events;
    private ClassicTCP tcp;
    private ClassicTCP host;


    @Before
    public void setup(){
        Util.resetTime();
        Util.setSeed(1337);
        this.events = new PriorityQueue<>();
        this.tcp = new ClassicTCP();
        this.host = new ClassicTCP();
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

        int numberOfRuns = 100;
        for (int i = 0; i < numberOfRuns; i++) {
            Util.resetTime();
            Util.setSeed(1337);

            this.tcp.send(new Message("hello"));
            this.events.add(new TCPSendEvent(this.tcp));

            String debugString = "Iteration: " + i;

            Event curEvent = this.events.poll();
            Assert.assertEquals(debugString, TCPSendEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, TCPSendEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, ChannelEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, TCPInputEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, TCPSendEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, ChannelEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, TCPInputEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, TCPSendEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertEquals(debugString, TCPRetransmitEventGenerator.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            Assert.assertEquals(debugString, 0, this.events.size());

        }
    }




}
