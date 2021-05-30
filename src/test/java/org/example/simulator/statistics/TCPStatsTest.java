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
import org.junit.Ignore;
import org.junit.Test;

public class TCPStatsTest {

    @Before
    public void setup() {
        Util.resetTime();
        Util.setSeed(1);
    }

    @Test
    public void floodWithPacketsInBigCongestedNetworkShouldWorkTest() {
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(10).setReno().withAddress(new SimpleAddress("Client")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.91).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.91).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.91).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.91).withAddress(new SimpleAddress("Router 4")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server")).build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().withLoss(0.01).build(r2, r3);
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


        //System.out.println(r1.getStats());
        /*
        r1.getStats().createArrivalChart();
        r1.getStats().createDepartureChart();
        r1.getStats().createTimeInSystemChart();
        r1.getStats().createInterArrivalChart();
        r1.getStats().createNumberOfPacketsInSystemChart();

         */
        ((RouterStats) r1.getStats()).createQueueSizeChart();

    }

    @Test
    public void MPTCPFloodWithPacketsInOrderShouldWorkTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(60).withAddress(new SimpleAddress("MPTCP-Client")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.94).withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.94).withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.9).withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(60).withAddress(new SimpleAddress("MPTCP-Server")).build();

        //path one
        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().withLoss(0.015).build(r1, server);

        //path two
        new Channel.ChannelBuilder().build(client, r2);
        new Channel.ChannelBuilder().withLoss(0.015).build(r2, server);

        //path three
        new Channel.ChannelBuilder().build(client, r3);
        new Channel.ChannelBuilder().withLoss(0.005).build(r3, server);

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
        for (TCPStats stat : server.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createArrivalChart();
            stat.createDepartureChart();
            stat.createInterArrivalChart();
            stat.createTimeInSystemChart();
            stat.createNumberOfPacketsInSystemChart();
        }

        //sender
        for (TCPStats stat : client.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createCWNDChart();
        }

    }


    @Test
    @Ignore
    public void MPTCPRealistic2HomogeneousDisconnectedFlowsWithDifferentTrafficParameters() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(20).withAddress(new SimpleAddress("MPTCP-Client")).build();

        Routable r11 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("A1")).build();
        Routable r12 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("A2")).build();
        Routable r13 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("A3")).build();
        Routable r14 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("A4")).build();

        Routable r21 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("B1")).build();
        Routable r22 = new Router.RouterBuilder().withAverageQueueUtilization(0.85).withAddress(new SimpleAddress("B2")).build();
        Routable r23 = new Router.RouterBuilder().withAverageQueueUtilization(0.85).withAddress(new SimpleAddress("B3")).build();
        Routable r24 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("B4")).build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(20).withAddress(new SimpleAddress("MPTCP-Server")).build();

        //path one
        new Channel.ChannelBuilder().withLoss(2.8).build(client, r11);
        new Channel.ChannelBuilder().build(r11, r12);
        new Channel.ChannelBuilder().build(r12, r13);
        new Channel.ChannelBuilder().build(r13, r14);
        new Channel.ChannelBuilder().withLoss(2.8).build(r14, server);

        //path two
        new Channel.ChannelBuilder().withLoss(2.8).build(client, r21);
        new Channel.ChannelBuilder().build(r21, r22);
        new Channel.ChannelBuilder().build(r22, r23);
        new Channel.ChannelBuilder().build(r23, r24);
        new Channel.ChannelBuilder().withLoss(2.8).build(r24, server);

        client.updateRoutingTable();

        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r13.updateRoutingTable();
        r14.updateRoutingTable();

        r21.updateRoutingTable();
        r22.updateRoutingTable();
        r23.updateRoutingTable();
        r24.updateRoutingTable();

        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        System.out.println("connected");

        Assert.assertTrue(client.getSubflows()[0].isConnected());
        Assert.assertTrue(client.getSubflows()[1].isConnected());

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
        Assert.assertTrue(r11.inputBufferIsEmpty());
        Assert.assertTrue(r12.inputBufferIsEmpty());
        Assert.assertTrue(r13.inputBufferIsEmpty());
        Assert.assertTrue(r14.inputBufferIsEmpty());
        Assert.assertTrue(r21.inputBufferIsEmpty());
        Assert.assertTrue(r22.inputBufferIsEmpty());
        Assert.assertTrue(r23.inputBufferIsEmpty());
        Assert.assertTrue(r24.inputBufferIsEmpty());

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals("iteration " + i, received.getPayload(), msg);
        }
        Assert.assertNull(server.receive());

        eventHandler.printStatistics();

        //receiver
        for (TCPStats stat : server.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createArrivalChart();
            stat.createDepartureChart();
            stat.createInterArrivalChart();
            stat.createTimeInSystemChart();
        }

        //sender
        for (TCPStats stat : client.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createCWNDChart();
        }

    }

}
