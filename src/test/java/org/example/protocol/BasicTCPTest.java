package org.example.protocol;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.simulator.EventHandler;
import org.example.simulator.events.ConnectEvent;
import org.example.simulator.events.RouteEvent;
import org.example.simulator.events.SendEvent;
import org.example.simulator.events.SendPacketEvent;

import org.example.simulator.events.run.RunTCPEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static java.lang.Thread.sleep;


public class BasicTCPTest {


    private Random RANDOM_GENERATOR;


    @Before
    public void setup(){
        RANDOM_GENERATOR = new Random();
    }


    @Test
    public void connectToEndpointTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        Assert.assertEquals(server, client.getConnection().getConnectedNode());
        Assert.assertEquals(client, server.getConnection().getConnectedNode());

        Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());
    }

    @Test
    public void connectThenSendMsgWorksTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        Message msg = new Message( "hello på do!");

        eventHandler.addEvent(new SendEvent(client, msg));
        eventHandler.run();

        Assert.assertEquals(msg, server.receive().getPayload());

    }


    @Test
    public void connectThenSendMsgOverMultipleNodesLineWorksTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();

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

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        Message msg = new Message( "hello på do!");

        eventHandler.addEvent(new SendEvent(client, msg));
        eventHandler.run();

        Assert.assertEquals(msg, server.receive().getPayload());

    }


    @Test
    public void packetsAreOrderedTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        for (int i = 0; i <= server.getWindowSize() * 2; i++) {
            Message msg = new Message( "test " + i);
            eventHandler.addEvent(new SendEvent(client, msg));
            eventHandler.run();
            Assert.assertEquals(msg, server.receive().getPayload());
        }
    }

    @Test
    public void unorderedPacketsAreNotReceivedTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        Message msg = new Message( "test1");
        eventHandler.addEvent(new SendEvent(client, msg));

        Message msg2 = new Message( "test2");
        Packet packet = new PacketBuilder()
                .withPayload(msg2)
                .withOrigin(client)
                .withDestination(server)
                .withSequenceNumber(client.getConnection().getNextSequenceNumber() + 100)
                .build();

        eventHandler.addEvent(new RouteEvent(client, packet));

        eventHandler.run();

        Assert.assertEquals(msg, server.receive().getPayload());
        Assert.assertNull(server.receive());
    }



    @Test
    public void unorderedPacketsAreDroppedAndOrderedPacketsAreReceivedWithoutBlockTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();

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

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();


        for (int i = 0; i < server.getWindowSize(); i++) {
            Packet packet1 = new PacketBuilder()
                    .withSequenceNumber(client.getConnection().getNextSequenceNumber() + 5000 + i)
                    .withAcknowledgmentNumber(client.getConnection().getNextAcknowledgementNumber() + 20000 + i)
                    .withOrigin(client)
                    .withDestination(server)
                    .withPayload(new Message("should not be received 1"))
                    .build();
            eventHandler.addEvent(new RouteEvent(client, packet1));

            Message msg = new Message( "test " + i);
            eventHandler.addEvent(new SendEvent(client, msg));

            Packet packet2 = new PacketBuilder()
                    .withSequenceNumber(client.getConnection().getNextSequenceNumber() + 100 + i)
                    .withAcknowledgmentNumber(client.getConnection().getNextAcknowledgementNumber() + 20 + i)
                    .withOrigin(client)
                    .withDestination(server)
                    .withPayload(new Message("should not be received 2"))
                    .build();
            eventHandler.addEvent(new RouteEvent(client, packet2));

            eventHandler.run();
        }

        for (int i = 0; i < server.getWindowSize(); i++) {
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals("test " + i, received.getPayload().toString());
        }

        Packet received = server.receive();
        Assert.assertNull(received);
    }


    @Test
    public void routedMessagesUnorderedReceiveOrderedTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();

        for (int i = client.getWindowSize() - 1; i >= 0 ; i--) {
            Packet packet = new PacketBuilder()
                    .withOrigin(client)
                    .withDestination(server)
                    .withSequenceNumber(seqNum + i)
                    .withAcknowledgmentNumber(ackNum + i)
                    .withPayload(new Message(i + ""))
                    .build();
            eventHandler.addEvent(new RouteEvent(client, packet));
            eventHandler.run();
        }

        for (int i = 0; i < client.getWindowSize(); i++) {
            Packet received = server.receive();
            Assert.assertNotNull("iteration: " + i, received);
            Assert.assertEquals(i + "", received.getPayload().toString());
        }
        Packet received = server.receive();
        Assert.assertNull(received);
    }


    @Test
    public void routeToManyMessagesUnorderedReceiveOrderedAndDropCorrectTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();


        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();

        for (int i = client.getWindowSize()*2; i >= 0 ; i--) {
            eventHandler.addEvent(new RouteEvent(client, new PacketBuilder()
                    .withOrigin(client)
                    .withDestination(server)
                    .withSequenceNumber(seqNum + i)
                    .withAcknowledgmentNumber(ackNum + i)
                    .withPayload(new Message(i + ""))
                    .build()
            ));
            eventHandler.run();
        }

        for (int i = 0; i < client.getWindowSize(); i++) {
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(i + "", received.getPayload().toString());
        }
        Packet received = server.receive();
        Assert.assertNull(received);
    }

    @Test
    public void packetIndexShouldUpdateAfterReceivingPacketInOrderTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();


        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        Packet packet = new PacketBuilder()
                .withConnection(client.getConnection())
                .withSequenceNumber(client.getConnection().getNextSequenceNumber())
                .build();

        int indexBeforeSending = server.receivingPacketIndex(packet);
        Assert.assertEquals(0, indexBeforeSending);

        eventHandler.addEvent(new SendPacketEvent(client, packet));
        eventHandler.run();
        Assert.assertNotNull(server.receive());

        int indexAfterReceived = server.receivingPacketIndex(packet);
        Assert.assertEquals(-1, indexAfterReceived);
    }

    @Test
    public void packetIndexShouldNotUpdateAfterReceivingPacketOutOfOrderButInWindowTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();


        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        //this test does not make sens for window size = 1
        if (client.getWindowSize() <= 1) return;

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();


        Packet packet = new PacketBuilder()
                .withConnection(client.getConnection())
                .withSequenceNumber(seqNum + client.getWindowSize()-1)
                .withAcknowledgmentNumber(ackNum + client.getWindowSize()-1)
                .build();

        int indexBeforeSending = server.receivingPacketIndex(packet);
        Assert.assertEquals(client.getWindowSize()-1, indexBeforeSending);

        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();
        Assert.assertNotNull(server.receive());

        int indexAfterReceived = server.receivingPacketIndex(packet);
        Assert.assertEquals(client.getWindowSize()-1, indexAfterReceived);
    }

    @Test
    public void inWindowShouldWorkOnPacketsThatShouldBeInWindowTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();

        for (int i = 0; i < client.getWindowSize(); i++) {
            Packet packet = new PacketBuilder()
                    .withOrigin(client)
                    .withDestination(server)
                    .withSequenceNumber(seqNum + i)
                    .withAcknowledgmentNumber(ackNum + i)
                    .build();
            Assert.assertTrue(server.inReceivingWindow(packet));
        }
    }

    @Test
    public void inWindowShouldNotWorkOnPacketsThatShouldNotBeInWindowTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();


        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();

        for (int i = 0; i < client.getWindowSize(); i++) {
            Packet packet = new PacketBuilder()
                    .withOrigin(client)
                    .withDestination(server)
                    .withSequenceNumber(seqNum + i + 1000)
                    .withAcknowledgmentNumber(ackNum + i + 1000)
                    .build();
            Assert.assertFalse(server.inReceivingWindow(packet));
        }
    }

    @Test
    public void floodWithPacketsInOrderShouldWorkTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable r1 = new Router.RouterBuilder().build();
        Routable r2 = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);
        r2.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        int numPacketsToSend = server.getWindowSize() * 100;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            eventHandler.addEvent(new SendEvent(client, msg));
            //sleep because events are incorrectly ordered in time when things happen fast
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        eventHandler.run();

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }
    }

    @Test
    public void floodWithPacketsInOrderThenWaitTilAllPacketsHasArrivedShouldWorkTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();


        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        int numPacketsToSend = server.getWindowSize() * 100;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }
    }


    @Test
    public void floodWithPacketsInOrderButInLossyChannelShouldWorkTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Routable router = new Router.RouterBuilder().withNoiseTolerance(2.5).build();
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new ConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        int numPacketsToSend = server.getWindowSize() * 100;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        eventHandler.addEvent(new RunTCPEvent(client));
        eventHandler.run();

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            System.out.println(received);
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }
    }




}
