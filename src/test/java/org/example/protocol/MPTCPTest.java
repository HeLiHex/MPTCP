package org.example.protocol;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.data.Payload;
import org.example.network.Routable;
import org.example.network.RoutableEndpoint;
import org.example.network.Router;

import org.example.network.address.SimpleAddress;
import org.example.simulator.EventHandler;
import org.example.simulator.events.RouteEvent;
import org.example.simulator.events.tcp.RunTCPEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;

import org.example.simulator.events.tcp.TCPSendEvent;
import org.example.util.Util;
import org.junit.*;
import org.junit.rules.Timeout;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MPTCPTest {

    @Rule
    public Timeout globalTimeout = new Timeout(30, TimeUnit.SECONDS);

    @Before
    public void setup(){
        Util.setSeed(1337);
        Util.resetTime();
    }
/*
    @Test(expected = IllegalArgumentException.class)
    public void mptcpThrowExceptionIfReceivingCapacityListLengthNotEqualToNumberOfSubflowsTest(){
        new MPTCP(2, 7);
    }

 */

    @Test
    @Ignore
    public void mptcpWithOneSubflowRoutingPacketRoutsItToItsDestinationStraitLine(){
        MPTCP client = new MPTCP(1, 7);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        RoutableEndpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100),100);

        client.addChannel(r1);
        r1.addChannel(r2);
        r2.addChannel(r3);
        r3.addChannel(r4);
        r4.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = server.getReceivedPacket();
        Assert.assertNotNull(receivedPacket);

        Payload receivedPayload = receivedPacket.getPayload();
        Assert.assertEquals(receivedPayload, msg);
    }

    @Test
    @Ignore
    public void mptcpWithTwoSubflowsRoutingPacketRoutsItToItsDestinationStraitLine(){
        MPTCP client = new MPTCP(2, 14);

        //path one
        Router r11 = new Router.RouterBuilder().build();
        Router r12 = new Router.RouterBuilder().build();
        Router r13 = new Router.RouterBuilder().build();
        Router r14 = new Router.RouterBuilder().build();

        //path two
        Router r21 = new Router.RouterBuilder().build();
        Router r22 = new Router.RouterBuilder().build();
        Router r23 = new Router.RouterBuilder().build();
        Router r24 = new Router.RouterBuilder().build();

        RoutableEndpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100),100);
        //path one
        client.addChannel(r11);
        r11.addChannel(r12);
        r12.addChannel(r13);
        r13.addChannel(r14);
        r14.addChannel(server);

        //path two
        client.addChannel(r21);
        r21.addChannel(r22);
        r22.addChannel(r23);
        r23.addChannel(r24);
        r24.addChannel(server);

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

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = server.getReceivedPacket();
        Assert.assertNotNull(receivedPacket);

        Payload receivedPayload = receivedPacket.getPayload();
        Assert.assertEquals(receivedPayload, msg);
    }

    @Test
    @Ignore
    public void mptcpWithTwoSubFlowsAndNonDistinctPathRoutsCorrectTest(){
        MPTCP client = new MPTCP(2, 14);

        Router r11 = new Router.RouterBuilder().build();
        Router r12 = new Router.RouterBuilder().build();

        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();

        RoutableEndpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100),100);
        //path one
        client.addChannel(r11);
        client.addChannel(r12);
        r11.addChannel(r3);
        r12.addChannel(r3);
        r3.addChannel(r4);
        r4.addChannel(server);

        client.updateRoutingTable();
        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = server.getReceivedPacket();
        Assert.assertNotNull(receivedPacket);

        Payload receivedPayload = receivedPacket.getPayload();
        Assert.assertEquals(receivedPayload, msg);
    }
/*
    @Test
    public void MPTCPConnectToEndpointTest(){
        MPTCP client = new MPTCP(2, 7, 7);
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        //Assert.assertEquals(server, client.getConnection().getConnectedNode());
        //Assert.assertEquals(client, server.getConnection().getConnectedNode());

        //Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());
        //Assert.assertTrue(client.getSubflows()[0].isConnected());
        //Assert.assertFalse(client.getSubflows()[1].isConnected());
    }

    @Test
    public void MPTCPOverlappingPathConnectToEndpointTest(){
        MPTCP client = new MPTCP(2, 7, 7);

        Router r11 = new Router.RouterBuilder().build();
        Router r12 = new Router.RouterBuilder().build();

        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();

        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        //path one
        client.addChannel(r11);
        client.addChannel(r12);
        r11.addChannel(r3);
        r12.addChannel(r3);
        r3.addChannel(r4);
        r4.addChannel(server);

        client.updateRoutingTable();
        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        //Assert.assertEquals(server, client.getConnection().getConnectedNode());
        //Assert.assertEquals(client, server.getConnection().getConnectedNode());

        Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());

        Assert.assertTrue(client.getSubflows()[0].isConnected());
        Assert.assertFalse(client.getSubflows()[1].isConnected());
    }

    @Test
    public void MPTCPConnectThenSendMsgOverOneSubflowsTest(){
        MPTCP client = new MPTCP(2, 7, 7);
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Message msg = new Message( "hello på do!");

        client.send(msg);
        eventHandler.addEvent(new TCPSendEvent(client));
        eventHandler.run();

        Packet received = server.receive();
        Assert.assertNotNull(received);
        Assert.assertEquals(msg, received.getPayload());
    }


    @Test
    public void MPTCPConnectThenSendMsgOverTwoSubflowsTest(){
        MPTCP client = new MPTCP(2, 7, 7);
        Routable r1 = new Router.RouterBuilder().build();
        Routable r2 = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        //path one
        client.addChannel(r1);
        r1.addChannel(server);

        //path two
        client.addChannel(r2);
        r2.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        //Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());
        Assert.assertTrue(client.getSubflows()[0].isConnected());
        Assert.assertFalse(client.getSubflows()[1].isConnected());

        Message msg1 = new Message( "hello 1!");
        Message msg2 = new Message( "hello 2!");

        client.send(msg1);
        client.send(msg2);
        eventHandler.addEvent(new TCPSendEvent(client));
        eventHandler.run();

        Packet received1 = server.receive();
        Assert.assertNotNull(received1);
        Assert.assertEquals(msg1, received1.getPayload());

        Packet received2 = server.receive();
        Assert.assertNotNull(received2);
        Assert.assertEquals(msg2, received2.getPayload());
    }
*/

    @Test
    public void MPTCPConnectToMPTCPThenSendMsgOverThreeSubflowsTest(){
        MPTCP client = new MPTCP(3, 21);
        Routable r1 = new Router.RouterBuilder().withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP(3, 21);

        //path one
        client.addChannel(r1);
        r1.addChannel(server);

        //path two
        client.addChannel(r2);
        r2.addChannel(server);

        //path three
        client.addChannel(r3);
        r3.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        System.out.println("connected");

        //Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());
        //Assert.assertEquals(server.getSubflows()[0].getConnection().getNextSequenceNumber() , client.getSubflows()[1].getConnection().getNextAcknowledgementNumber());

        Assert.assertTrue(client.getSubflows()[0].isConnected());
        Assert.assertTrue(client.getSubflows()[1].isConnected());
        Assert.assertTrue(client.getSubflows()[2].isConnected());

        Message msg1 = new Message( "hello 1!");
        Message msg2 = new Message( "hello 2!");
        Message msg3 = new Message( "hello 3!");

        client.send(msg1);
        client.send(msg2);
        client.send(msg3);
        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        Packet received1 = server.receive();
        Assert.assertNotNull(received1);
        System.out.println(received1.getIndex());
        System.out.println(received1.getPayload());
        Assert.assertEquals(msg1, received1.getPayload());

        Packet received2 = server.receive();
        Assert.assertNotNull(received2);
        System.out.println(received2.getIndex());
        System.out.println(received2.getPayload());
        Assert.assertEquals(msg2, received2.getPayload());

        Packet received3 = server.receive();
        Assert.assertNotNull(received3);
        System.out.println(received3.getIndex());
        System.out.println(received3.getPayload());
        Assert.assertEquals(msg3, received3.getPayload());

        Assert.assertNull(server.receive());
    }


    @Test
    public void MPTCPFloodWithPacketsInOrderShouldWorkTest(){
        MPTCP client = new MPTCP(3, 21);
        Routable r1 = new Router.RouterBuilder().withNoiseTolerance(2.2).withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withNoiseTolerance(2.2).withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withNoiseTolerance(2.2).withAddress(new SimpleAddress("C")).build();
        MPTCP server = new MPTCP(3, 21);

        //path one
        client.addChannel(r1);
        r1.addChannel(server);

        //path two
        client.addChannel(r2);
        r2.addChannel(server);

        //path three
        client.addChannel(r3);
        r3.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        System.out.println("connected");

        //Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());
        //Assert.assertEquals(server.getSubflows()[0].getConnection().getNextSequenceNumber() , client.getSubflows()[1].getConnection().getNextAcknowledgementNumber());

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

        //Assert.assertTrue(client.inputBufferIsEmpty());
        //Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r1.inputBufferIsEmpty());
        Assert.assertTrue(r2.inputBufferIsEmpty());
        Assert.assertTrue(r3.inputBufferIsEmpty());



        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            System.out.println("received " + received);
            //Assert.assertNotNull(received);
            //Assert.assertEquals("iteration " + i, received.getPayload(), msg);
        }
    }



}
