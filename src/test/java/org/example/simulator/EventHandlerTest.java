package org.example.simulator;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Router;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ConnectEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.SendEvent;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Queue;
import java.util.Random;

import static java.lang.Thread.holdsLock;
import static java.lang.Thread.sleep;

public class EventHandlerTest {


    private static final Random RANDOM_GENERATOR = new Random(69);

    private synchronized Packet getPacket(TCP endpoint){
        for (int i = 0; i < 1000; i++) {
            Packet packet = endpoint.receive();
            if (packet != null) return packet;
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


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
        eventHandler.run();
        eventHandler.run();
        eventHandler.run();
    }



    @Test
    public void runTest(){
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(2).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();


        eventHandler.addEvent(new SendEvent(client, new Message("test")));
        eventHandler.run();


        int multiplier = 100;
        int numPacketsToSend = server.getWindowSize() * multiplier;

    }


}
