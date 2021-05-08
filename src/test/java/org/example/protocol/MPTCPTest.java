package org.example.protocol;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.network.address.SimpleAddress;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.RunTCPEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.util.Util;
import org.junit.*;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

public class MPTCPTest {

    @Rule
    public Timeout globalTimeout = new Timeout(120, TimeUnit.SECONDS);

    @Before
    public void setup() {
        Util.setSeed(1337);
        Util.resetTime();
    }

    @Test
    public void MPTCPWithTwoSubFlowsAndNonDistinctPathConnectAndSendCorrectTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(14).withAddress(new SimpleAddress("MPTCP-Client")).build();

        Router r11 = new Router.RouterBuilder().build();
        Router r12 = new Router.RouterBuilder().build();

        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(14).withAddress(new SimpleAddress("MPTCP-Server")).build();

        new Channel.ChannelBuilder().build(client, r11);
        new Channel.ChannelBuilder().build(client, r12);
        new Channel.ChannelBuilder().build(r11, r3);
        new Channel.ChannelBuilder().build(r12, r3);
        new Channel.ChannelBuilder().build(r3, r4);
        new Channel.ChannelBuilder().build(r4, server);
        new Channel.ChannelBuilder().build(r4, server);

        client.updateRoutingTable();
        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        System.out.println("connected");


        Message msg = new Message("hello på do!");
        client.send(msg);
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Packet receivedPacket = server.receive();
        Assert.assertNotNull(receivedPacket);

        Payload receivedPayload = receivedPacket.getPayload();
        Assert.assertEquals(receivedPayload, msg);

        Assert.assertNull(server.receive());
    }

    @Test
    public void MPTCPConnectToTCPTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(14).withAddress(new SimpleAddress("MPTCP-Client")).build();
        Routable router = new Router.RouterBuilder().withAddress(new SimpleAddress("router")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withAddress(new SimpleAddress("server")).withReceivingWindowCapacity(7).build();

        new Channel.ChannelBuilder().build(client, router);
        new Channel.ChannelBuilder().build(client, router);
        new Channel.ChannelBuilder().build(router, server);
        new Channel.ChannelBuilder().build(router, server);


        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        System.out.println(client.getChannels());
        System.out.println(router.getChannels());
        System.out.println(server.getChannels());

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertFalse(client.isConnected());
        Assert.assertTrue(client.getSubflows()[0].isConnected());
        Assert.assertFalse(client.getSubflows()[1].isConnected());
    }


    @Test
    public void MPTCPConnectThenSendMsgOverTwoSubflowsTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(14).withAddress(new SimpleAddress("MPTCP-Client")).build();
        Routable r1 = new Router.RouterBuilder().build();
        Routable r2 = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        //path one
        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, server);

        //path two
        new Channel.ChannelBuilder().build(client, r2);
        new Channel.ChannelBuilder().build(r2, server);


        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertTrue(client.getSubflows()[0].isConnected());
        Assert.assertFalse(client.getSubflows()[1].isConnected());

        Message msg1 = new Message("hello 1!");
        Message msg2 = new Message("hello 2!");

        client.send(msg1);
        client.send(msg2);
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Packet received1 = server.receive();
        Assert.assertNotNull(received1);
        Assert.assertEquals(msg1, received1.getPayload());

        Packet received2 = server.receive();
        Assert.assertNotNull(received2);
        Assert.assertEquals(msg2, received2.getPayload());
    }


    @Test
    public void MPTCPConnectToMPTCPNonDistinctPathTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(20).withAddress(new SimpleAddress("MPTCP-Client")).build();
        Routable r1 = new Router.RouterBuilder().withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(20).withAddress(new SimpleAddress("MPTCP-Server")).build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(client, r2);

        new Channel.ChannelBuilder().build(r3, r1);
        new Channel.ChannelBuilder().build(r3, r2);

        new Channel.ChannelBuilder().build(server, r3);
        new Channel.ChannelBuilder().build(server, r3);


        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        System.out.println("connected");

        Assert.assertTrue(client.isConnected());

        Message msg1 = new Message("hello 1!");
        Message msg2 = new Message("hello 2!");
        Message msg3 = new Message("hello 3!");

        client.send(msg1);
        client.send(msg2);
        client.send(msg3);
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Packet received1 = server.receive();
        Assert.assertNotNull(received1);
        Assert.assertEquals(msg1, received1.getPayload());

        Packet received2 = server.receive();
        Assert.assertNotNull(received2);
        Assert.assertEquals(msg2, received2.getPayload());

        Packet received3 = server.receive();
        Assert.assertNotNull(received3);
        Assert.assertEquals(msg3, received3.getPayload());

        Assert.assertNull(server.receive());
    }

    @Test
    public void MPTCPConnectToMPTCPThenSendMsgOverThreeSubflowsTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Client")).build();
        Routable r1 = new Router.RouterBuilder().withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Server")).build();

        //path one
        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, server);

        //path two
        new Channel.ChannelBuilder().build(client, r2);
        new Channel.ChannelBuilder().build(r2, server);

        //path three
        new Channel.ChannelBuilder().build(client, r3);
        new Channel.ChannelBuilder().build(r3, server);



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

        Message msg1 = new Message("hello 1!");
        Message msg2 = new Message("hello 2!");
        Message msg3 = new Message("hello 3!");

        client.send(msg1);
        client.send(msg2);
        client.send(msg3);
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Packet received1 = server.receive();
        Assert.assertNotNull(received1);
        Assert.assertEquals(msg1, received1.getPayload());

        Packet received2 = server.receive();
        Assert.assertNotNull(received2);
        Assert.assertEquals(msg2, received2.getPayload());

        Packet received3 = server.receive();
        Assert.assertNotNull(received3);
        Assert.assertEquals(msg3, received3.getPayload());

        Assert.assertNull(server.receive());
    }


    @Test
    public void MPTCPFloodWithPacketsInOrderShouldWorkTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Client")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.8).withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Server")).build();

        //path one
        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, server);

        //path two
        new Channel.ChannelBuilder().build(client, r2);
        new Channel.ChannelBuilder().build(r2, server);

        //path three
        new Channel.ChannelBuilder().build(client, r3);
        new Channel.ChannelBuilder().build(r3, server);


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

    }

    @Test
    public void MPTCPFloodWithPacketsInLossyChannelsShouldWorkTest() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Client")).build();
        Routable r1 = new Router.RouterBuilder().withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(3).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Server")).build();

        //path one
        new Channel.ChannelBuilder().withLoss(0.001).build(client, r1);
        new Channel.ChannelBuilder().withLoss(0.01).build(r1, server);

        //path two
        new Channel.ChannelBuilder().withLoss(0.001).build(client, r2);
        new Channel.ChannelBuilder().withLoss(0.001).build(r2, server);

        //path three
        new Channel.ChannelBuilder().withLoss(0.01).build(client, r3);
        new Channel.ChannelBuilder().withLoss(0.001).build(r3, server);

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

        //todo - this fails bcuz of the retranmitt issue... I think. quick fix is to set noisetolerance to 2.5
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
    }


    @Test
    public void MPTCPFloodWithPacketsInOrderWithVariableNumberOfSubflowsShouldWorkTest() {
        int maxSubflows = 10;
        for (int numSubflows = 1; numSubflows <= maxSubflows; numSubflows++) {
            Util.setSeed(1337);
            Util.resetTime();
            MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(numSubflows).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Client")).build();
            MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(numSubflows).withReceivingWindowCapacity(21).withAddress(new SimpleAddress("MPTCP-Server")).build();

            for (int i = 1; i <= numSubflows; i++) {
                Routable router = new Router.RouterBuilder().withAddress(new SimpleAddress("Router " + i)).build();

                new Channel.ChannelBuilder().build(client, router);
                new Channel.ChannelBuilder().build(router, server);
                router.updateRoutingTable();
            }
            client.updateRoutingTable();
            server.updateRoutingTable();

            EventHandler eventHandler = new EventHandler();
            eventHandler.addEvent(new TCPConnectEvent(client, server));
            eventHandler.run();
            System.out.println("connected");

            for (TCP subflow : client.getSubflows()) {
                Assert.assertTrue(subflow.isConnected());
            }

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
            for (TCP subflow : client.getSubflows()) {
                Assert.assertTrue(subflow.inputBufferIsEmpty());
            }

            for (int i = 1; i <= numPacketsToSend; i++) {
                Message msg = new Message("test " + i);
                Packet received = server.receive();
                Assert.assertNotNull(received);
                Assert.assertEquals("iteration " + i, received.getPayload(), msg);
            }
            Assert.assertNull(server.receive());
        }
    }


}
