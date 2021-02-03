package org.example.simulator;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Router;
import org.example.protocol.BasicTCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.TCPEvents.TCPConnectEvent;
import org.example.simulator.events.TCPEvents.TCPInputEvent;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;


public class EventHandlerTest {

    @Test
    public void runRunsWithoutErrorTest(){
        EventHandler eventHandler = new EventHandler();
        Event eventOne = new Event(Instant.now()) {
            @Override
            public void run() {
                System.out.println(this.getInitInstant());
            }

            @Override
            public void generateNextEvent(Queue<Event> events) {
                events.add(new Event(Instant.now()) {
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

        Event eventTwo = new Event(Instant.now()) {
            @Override
            public void run() {
                System.out.println(this.getInitInstant());
            }

            @Override
            public void generateNextEvent(Queue<Event> events) {
                events.add(new Event(Instant.now()) {
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

        Awaitility.await().atLeast(Duration.FIVE_SECONDS);
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
    public void eventsArrangementAreConsistent(){
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

        int numPacketsToSend = 1000;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        ArrayDeque eventList = new ArrayDeque();

        while (eventHandler.peekEvent() != null){
            eventList.add(eventHandler.peekEvent());
            eventHandler.singleRun();
        }

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        while (eventHandler.peekEvent() != null){
            Assert.assertEquals(eventList.poll().getClass(), eventHandler.peekEvent().getClass());
            eventHandler.singleRun();
        }


    }

}
