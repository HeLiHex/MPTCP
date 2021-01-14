package org.example.simulator;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Router;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.Event;
import org.example.simulator.events.SendEvent;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Queue;
import java.util.Random;

import static java.lang.Thread.sleep;

public class EventHandlerTest {


    @Test
    public void runRunsWithoutErrorTest(){
        EventHandler eventHandler = new EventHandler();
        Event testEvent = new Event(Instant.now()) {
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
        eventHandler.addEvent(testEvent);
        eventHandler.addEvent(testEvent);

        Awaitility.await().atLeast(Duration.FIVE_SECONDS);
        eventHandler.run();
        eventHandler.run();
        eventHandler.run();
        eventHandler.run();
    }


}
