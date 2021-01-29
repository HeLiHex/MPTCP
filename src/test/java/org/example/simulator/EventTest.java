package org.example.simulator;

import org.example.data.PacketBuilder;
import org.example.network.RoutableEndpoint;
import org.example.network.Router;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.AbstractTCP;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.TCPEvents.TCPConnectEvent;
import org.example.simulator.events.Event;
import org.junit.Before;

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
        eventHandler.addEvent(new TCPConnectEvent(linkedClient, linkedServer));
        eventHandler.run();
    }




}
