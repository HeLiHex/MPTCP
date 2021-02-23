package org.example.simulator;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Router;
import org.example.protocol.BasicTCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;


public class EventHandlerTest {

    @Test
    public void runRunsWithoutErrorTest(){
        EventHandler eventHandler = new EventHandler();
        Event eventOne = new Event(Util.getTime()) {
            @Override
            public void run() {
                System.out.println(this.getInitInstant());
            }

            @Override
            public void generateNextEvent(Queue<Event> events) {
                events.add(new Event(Util.getTime()) {
                    @Override
                    public void run() {
                        System.out.println(this.getInitInstant());
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
                System.out.println(this.getInitInstant());
            }

            @Override
            public void generateNextEvent(Queue<Event> events) {
                events.add(new Event(Util.getTime()) {
                    @Override
                    public void run() {
                        System.out.println(this.getInitInstant());
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

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();
        Router r1 = new Router.RouterBuilder().build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();


        client.send(new Message("test"));
        eventHandler.addEvent(new TCPInputEvent(client));
        eventHandler.run();

        Assert.assertEquals(0, eventHandler.getNumberOfEvents());
    }

    @Test
    public void runFloodWithPacketsInOrderButInLossyChannelShouldWorkTest() {
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();
        Router r1 = new Router.RouterBuilder().build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        int multiplier = 100;
        int numPacketsToSend = server.getWindowSize() * multiplier;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));
        eventHandler.run();

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }
    }


    @Test
    public void eventArrangementsAreConsistent(){
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();
        Router r1 = new Router.RouterBuilder()
                .withNoiseTolerance(1)
                .build();

        client.addChannel(r1);
        r1.addChannel(server);

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
        eventHandler.addEvent(new TCPInputEvent(client));

        Deque<Event> eventList = new ArrayDeque<>();

        while (eventHandler.peekEvent() != null){
            eventList.add(eventHandler.peekEvent());
            eventHandler.singleRun();
        }

        Assert.assertNull(server.dequeueInputBuffer());
        Assert.assertNull(client.dequeueInputBuffer());
        Assert.assertNull(r1.dequeueInputBuffer());
        Assert.assertNull(eventHandler.peekEvent());

        Util.setSeed(1337);
        Util.resetTime();

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        while (eventHandler.peekEvent() != null){
            //System.out.println(eventList.peek().getClass().equals(eventHandler.peekEvent().getClass()));
            Event event = eventList.poll();
            if (event == null) Assert.fail();
            Assert.assertEquals(event.getClass(), eventHandler.peekEvent().getClass());
            eventHandler.singleRun();
        }


    }
}
