package org.example.simulator;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.network.Channel;
import org.example.protocol.ClassicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.RouteEvent;
import org.example.simulator.events.tcp.RunTCPEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPRetransmitEventGenerator;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.PriorityQueue;
import java.util.Queue;

public class EventTest {

    private Queue<Event> events;
    private ClassicTCP tcp;
    private ClassicTCP host;

    public void connect(TCP linkedClient, TCP linkedServer) {
        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(linkedClient, linkedServer));
        eventHandler.run();
        Assert.assertTrue(linkedClient.isConnected());
        Assert.assertTrue(linkedServer.isConnected());
    }

    @Test(expected = IllegalArgumentException.class)
    public void expectIllegalArgumentExceptionIfNullChannelGiven() {
        new ChannelEvent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void expectIllegalArgumentExceptionIfNullNodeGiven() {
        new RunTCPEvent(null);
    }

    @Test
    public void routeEventEqualsTest() {
        Event event1 = new RouteEvent(new ClassicTCP.ClassicTCPBuilder().build(), new PacketBuilder().build());
        Event event2 = new RouteEvent(new ClassicTCP.ClassicTCPBuilder().build(), new PacketBuilder().build());
        Assert.assertNotEquals(event1, event2);
    }


    @Test
    public void EventsOccurInAPreMatchedSequence() {
        int numberOfRuns = 100;
        for (int i = 0; i < numberOfRuns; i++) {
            Util.resetTime();
            Util.setSeed(1337);
            this.events = new PriorityQueue<>();
            this.tcp = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            this.host = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

            new Channel.ChannelBuilder().build(this.tcp, this.host);

            this.tcp.updateRoutingTable();
            this.host.updateRoutingTable();
            this.connect(this.tcp, this.host);

            this.tcp.send(new Message("hello"));
            this.events.add(new RunTCPEvent(this.tcp));

            String debugString = "Iteration: " + (i + 1);

            Event curEvent = this.events.poll();
            Assert.assertNotNull(curEvent);
            Assert.assertEquals(debugString, RunTCPEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertNotNull(curEvent);
            Assert.assertEquals(debugString, ChannelEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertNotNull(curEvent);
            Assert.assertEquals(debugString, RunTCPEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertNotNull(curEvent);
            Assert.assertEquals(debugString, ChannelEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertNotNull(curEvent);
            Assert.assertEquals(debugString, RunTCPEvent.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            curEvent = this.events.poll();
            Assert.assertNotNull(curEvent);
            Assert.assertEquals(debugString, TCPRetransmitEventGenerator.class, curEvent.getClass());
            curEvent.run();
            curEvent.generateNextEvent(this.events);

            Assert.assertTrue(debugString, this.tcp.outputBufferIsEmpty());
            Assert.assertTrue(debugString, this.host.outputBufferIsEmpty());
            Assert.assertTrue(debugString, this.tcp.inputBufferIsEmpty());
            Assert.assertTrue(debugString, this.host.inputBufferIsEmpty());
            Assert.assertEquals(debugString, 0, this.events.size());
        }
    }


}
