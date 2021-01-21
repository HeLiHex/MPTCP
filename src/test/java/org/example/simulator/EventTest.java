package org.example.simulator;

import org.example.network.RoutableEndpoint;
import org.example.network.Router;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ConnectEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.TCPevents.InputEvent;
import org.example.simulator.events.TCPevents.RetransmitEvent;
import org.example.simulator.events.TCPevents.TrySendEvent;
import org.example.simulator.events.run.RunNetworkNodeEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class EventTest {

    private Queue<Event> events;
    private AbstractTCP tcp;


    @Before
    public void setup(){
        this.events = new PriorityQueue<>();
        this.tcp = new BasicTCP(new Random());

    }

    public void connect(TCP linkedClient, Endpoint linkedServer){
        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(linkedClient, linkedServer));
        eventHandler.run();
    }

    @Test
    public void inputEventGeneratesRetransmitEventTest(){
        Event event = new InputEvent(this.tcp);
        event.run();
        event.generateNextEvent(this.events);
        Assert.assertEquals(RetransmitEvent.class, this.events.poll().getClass());
        Assert.assertNull(this.events.poll());
    }

    @Test
    public void retransmitEventGeneratesTrySendEventTest(){
        Event event = new RetransmitEvent(this.tcp);
        event.run();
        event.generateNextEvent(this.events);
        Assert.assertEquals(TrySendEvent.class, this.events.poll().getClass());
        Assert.assertNull(this.events.poll());
    }

    @Test
    public void trySendEventGeneratesRunNetworkNodeEventTest(){
        Endpoint server = new BasicTCP(new Random());
        Router router = new Router.RouterBuilder().build();

        this.tcp.addChannel(router);
        router.addChannel(server);

        this.tcp.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        this.connect(this.tcp, server);

        Event event = new TrySendEvent(this.tcp);
        event.run();
        event.generateNextEvent(this.events);
        Assert.assertEquals(RunNetworkNodeEvent.class, this.events.poll().getClass());
        Assert.assertNull(this.events.poll());
    }



}
