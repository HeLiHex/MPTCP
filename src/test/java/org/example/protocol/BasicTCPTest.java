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
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static java.lang.Thread.sleep;

public class BasicTCPTest {


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
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();

        client.connect(server);

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();


        for (int i = client.getWindowSize()*2; i >= 0 ; i--) {
            client.route(new PacketBuilder()
                    .withOrigin(client)
                    .withDestination(server)
                    .withSequenceNumber(seqNum + i)
                    .withAcknowledgmentNumber(ackNum + i)
                    .withPayload(new Message(i + ""))
                    .build()
            );
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < client.getWindowSize(); i++) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Packet received = getPacket(server);
            Assert.assertNotNull(received);
            Assert.assertEquals(i + "", received.getPayload().toString());
        }

        Packet received = getPacket(server);
        Assert.assertNull(received);
    }

    @Test
    public void packetIndexShouldUpdateAfterReceivingPacketInOrderTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

        Packet packet = new PacketBuilder()
                .withConnection(client.getConnection())
                .withSequenceNumber(client.getConnection().getNextSequenceNumber())
                .build();

        int indexBeforeSending = server.receivingPacketIndex(packet);
        Assert.assertEquals(0, indexBeforeSending);

        client.send(packet);
        getPacket(server);

        int indexAfterReceived = server.receivingPacketIndex(packet);
        Assert.assertEquals(-1, indexAfterReceived);
    }

    @Test
    public void packetIndexShouldNotUpdateAfterReceivingPacketOutOfOrderButInWindowTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

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

        client.route(packet);
        getPacket(server);

        int indexAfterReceived = server.receivingPacketIndex(packet);
        Assert.assertEquals(client.getWindowSize()-1, indexAfterReceived);
    }

    @Test
    public void inWindowShouldWorkOnPacketsThatShouldBeInWindowTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

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
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

        int numPacketsToSend = server.getWindowSize() * 100;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = getPacket(server);
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }

    }

    @Test
    public void floodWithPacketsInOrderThenWaitTilAllPacketsHasArrivedShouldWorkTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

        int numPacketsToSend = server.getWindowSize() * 100;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        while (!client.outputBufferIsEmpty() || client.hasWaitingPackets()){}

        System.out.println("should be last print");
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = getPacket(server);
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }

    }


    @Test
    public void floodWithPacketsInOrderButInLossyChannelShouldWorkTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(2).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        r1.start();
        server.start();

        client.connect(server);

        int multiplier = 100;
        int numPacketsToSend = server.getWindowSize() * multiplier;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        while (!client.outputBufferIsEmpty() || client.hasWaitingPackets()){}
        System.out.println("should be last print");

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = getPacket(server);
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }
    }




}
