package org.example.simulator;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Router;
import org.example.protocol.ClassicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class EventHandlerTest {

    @Rule
    public Timeout globalTimeout = new Timeout(30, TimeUnit.SECONDS);

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

        ClassicTCP client = new ClassicTCP();
        ClassicTCP server = new ClassicTCP();
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

        ClassicTCP client = new ClassicTCP();
        ClassicTCP server = new ClassicTCP();
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

    private ArrayList<Event> allEventsList(int numPacketsToSend, double noiseTolerance){
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP();
        ClassicTCP server = new ClassicTCP();
        Router r1 = new Router.RouterBuilder()
                .withNoiseTolerance(noiseTolerance)
                .build();

        client.addChannel(r1);
        r1.addChannel(server);

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
        eventHandler.addEvent(new TCPInputEvent(client));

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
        double noiseTolerance = 2;
        int numPacketsToSend = 1000;
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

        ClassicTCP client = new ClassicTCP();
        ClassicTCP server = new ClassicTCP();
        Router r1 = new Router.RouterBuilder()
                .withNoiseTolerance(noiseTolerance)
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
