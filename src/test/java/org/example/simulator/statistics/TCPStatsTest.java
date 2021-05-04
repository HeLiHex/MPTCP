package org.example.simulator.statistics;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.network.address.SimpleAddress;
import org.example.protocol.ClassicTCP;
import org.example.protocol.MPTCP;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.RunTCPEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TCPStatsTest {

    @Before
    public void setup(){
        Util.resetTime();
        Util.setSeed(1);
    }

    @Test
    public void floodWithPacketsInBigCongestedNetworkShouldWorkTest() {
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(10).setReno().withAddress(new SimpleAddress("Client")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server")).build();

        new Channel.ChannelBuilder().withNoiseTolerance(2).build(client, r1);
        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().build(r2, r3);
        new Channel.ChannelBuilder().build(r3, r4);
        new Channel.ChannelBuilder().build(r4, server);


        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(server.isConnected());

        System.out.println("connected");

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());

        int numPacketsToSend = 10000;
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }

        eventHandler.printStatistics();
        TCPStats stat = server.getStats();
        System.out.println(stat);
        stat.createArrivalChart();
        stat.createDepartureChart();
        stat.createInterArrivalChart();
        stat.createTimeInSystemChart();
        stat.createNumberOfPacketsInSystemChart();

        System.out.println(client.getStats().toString());
        client.getStats().createCWNDChart();


        System.out.println(r1.getStats());
        r1.getStats().createArrivalChart();
        r1.getStats().createDepartureChart();
        r1.getStats().createTimeInSystemChart();
        r1.getStats().createInterArrivalChart();
        r1.getStats().createNumberOfPacketsInSystemChart();
        ((RouterStats)r1.getStats()).createQueueSizeChart();

    }

    @Test
    public void MPTCPFloodWithPacketsInOrderShouldWorkTest() {
        MPTCP client = new MPTCP(3, 21);
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.86).withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.86).withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.86).withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP(3, 21);

        //path one
        new Channel.ChannelBuilder().withNoiseTolerance(2.6).build(client, r1);
        new Channel.ChannelBuilder().withNoiseTolerance(2.6).build(r1, server);

        //path two
        new Channel.ChannelBuilder().withNoiseTolerance(2.6).build(client, r2);
        new Channel.ChannelBuilder().withNoiseTolerance(2.6).build(r2, server);

        //path three
        new Channel.ChannelBuilder().withNoiseTolerance(2.6).build(client, r3);
        new Channel.ChannelBuilder().withNoiseTolerance(2.6).build(r3, server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        System.out.println("connected");

        Assert.assertTrue(client.getSubflows()[0].isConnected());
        Assert.assertTrue(client.getSubflows()[1].isConnected());
        Assert.assertTrue(client.getSubflows()[2].isConnected());

        int numPacketsToSend = 10000;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Assert.assertTrue("client still has packets to send", client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r1.inputBufferIsEmpty());
        Assert.assertTrue(r2.inputBufferIsEmpty());
        Assert.assertTrue(r3.inputBufferIsEmpty());


        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals("iteration " + i, received.getPayload(), msg);
        }
        Assert.assertNull(server.receive());

        eventHandler.printStatistics();

        //receiver
        for (TCPStats stat: server.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createArrivalChart();
            stat.createDepartureChart();
            stat.createInterArrivalChart();
            stat.createTimeInSystemChart();
        }

        //sender
        for (TCPStats stat: client.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createCWNDChart();
        }

    }

}
