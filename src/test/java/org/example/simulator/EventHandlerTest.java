package org.example.simulator;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.Router;
import org.example.protocol.ClassicTCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.RouteEvent;
import org.example.simulator.events.tcp.RunTCPEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


public class EventHandlerTest {

    @Rule
    public Timeout globalTimeout = new Timeout(30, TimeUnit.SECONDS);

    @Before
    public void setup(){
        Util.setSeed(1337);
        Util.resetTime();
    }

    @Test
    public void runRunsWithoutErrorTest(){
        EventHandler eventHandler = new EventHandler();
        Event eventOne = new Event(Util.getTime()) {
            @Override
            public void run() {
                System.out.println(this.getInstant());
            }

            @Override
            public void generateNextEvent(Queue<Event> events) {
                events.add(new Event(Util.getTime()) {
                    @Override
                    public void run() {
                        System.out.println(this.getInstant());
                    }

                    @Override
                    public void generateNextEvent(Queue<Event> events) {

                    }
                });
            }
        };

        Event eventTwo = new Event(Util.getTime()) {
            @Override
            public void run() {
                System.out.println(this.getInstant());
            }

            @Override
            public void generateNextEvent(Queue<Event> events) {
                events.add(new Event(Util.getTime()) {
                    @Override
                    public void run() {
                        System.out.println(this.getInstant());
                    }

                    @Override
                    public void generateNextEvent(Queue<Event> events) {

                    }
                });
            }
        };

        //start condition
        eventHandler.addEvent(eventOne);
        eventHandler.addEvent(eventTwo);

        eventHandler.run();

        Assert.assertEquals(0, eventHandler.getNumberOfEvents());
    }



    @Test
    public void runTest(){
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Router r1 = new Router.RouterBuilder().build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();


        client.send(new Message("test"));
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Assert.assertEquals(0, eventHandler.getNumberOfEvents());
    }

    @Test
    public void runFloodWithPacketsInOrderButInLossyChannelShouldWorkTest() {
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Router r1 = new Router.RouterBuilder().build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        int multiplier = 100;
        int numPacketsToSend = server.getThisReceivingWindowCapacity() * multiplier;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }
    }

    private ArrayList<Event> allEventsList(int numPacketsToSend, double noiseTolerance){
        Util.setSeed(1337);
        Util.resetTime();
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Router r1 = new Router.RouterBuilder().build();

        new Channel.ChannelBuilder().withNoiseTolerance(noiseTolerance).build(client, r1);
        new Channel.ChannelBuilder().withNoiseTolerance(noiseTolerance).build(r1, server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertNull(eventHandler.peekEvent());

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new RunTCPEvent(client));

        ArrayList<Event> eventList = new ArrayList<>();

        while (eventHandler.peekEvent() != null){
            eventList.add(eventHandler.peekEvent());
            eventHandler.singleRun();
        }

        Assert.assertNull(server.dequeueInputBuffer());
        Assert.assertNull(client.dequeueInputBuffer());
        Assert.assertNull(r1.dequeueInputBuffer());
        Assert.assertNull(eventHandler.peekEvent());

        return eventList;
    }

    @Test
    public void eventArrangementsAreConsistent(){
        double noiseTolerance = 2.5;
        int numPacketsToSend = 1001;
        ArrayList<Event> eventList1 = this.allEventsList(numPacketsToSend, noiseTolerance);
        ArrayList<Event> eventList2 = this.allEventsList(numPacketsToSend, noiseTolerance);

        for (Event event : eventList1){
            Assert.assertEquals(event.getClass(), eventList2.remove(0).getClass());
        }
    }


    @Test
    public void eventAreRunningInCorrectOrderWithRespectToTime() {
        double noiseTolerance = 2;
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Router r1 = new Router.RouterBuilder().build();

        new Channel.ChannelBuilder().withNoiseTolerance(noiseTolerance).build(client, r1);
        new Channel.ChannelBuilder().withNoiseTolerance(noiseTolerance).build(r1, server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertNull(eventHandler.peekEvent());

        Util.setSeed(1337);

        int numPacketsToSend = 1000;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new RunTCPEvent(client));

        Event prevEvent;
        while (true) {
            prevEvent = eventHandler.peekEvent();
            eventHandler.singleRun();

            if (eventHandler.peekEvent() == null) break;

            Event curEvent = eventHandler.peekEvent();
            Assert.assertTrue(prevEvent.getInstant() <= curEvent.getInstant());
        }
        Assert.assertNull(server.dequeueInputBuffer());
        Assert.assertNull(client.dequeueInputBuffer());
        Assert.assertNull(r1.dequeueInputBuffer());
        Assert.assertNull(eventHandler.peekEvent());
    }
}
