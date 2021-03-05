package org.example.protocol;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.simulator.EventHandler;
import org.example.simulator.events.RouteEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.simulator.events.tcp.TCPSendEvent;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class ClassicTCPTest {

    private int seconds(int seconds){
        return 1000 * seconds;
    }

    @Rule
    public Timeout globalTimeout = new Timeout(seconds(30));

    @Test
    public void connectToEndpointTest(){
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertEquals(server, client.getConnection().getConnectedNode());
        Assert.assertEquals(client, server.getConnection().getConnectedNode());

        Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber() - 1);
    }

    @Test
    public void connectThenSendMsgWorksTest(){
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

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
        eventHandler.addEvent(new TCPInputEvent(client));
        eventHandler.run();

        Assert.assertEquals(msg, server.receive().getPayload());

    }


    @Test
    public void connectThenSendMsgOverMultipleNodesLineWorksTest(){
        ClassicTCP client = new ClassicTCP();
        ClassicTCP server = new ClassicTCP();
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
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Message msg = new Message( "hello på do!");

        client.send(msg);
        eventHandler.addEvent(new TCPInputEvent(client));
        eventHandler.run();

        Assert.assertEquals(msg, server.receive().getPayload());

    }


    @Test
    public void packetsAreOrderedTest(){
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        for (int i = 0; i <= server.getWindowSize() * 2; i++) {
            Message msg = new Message( "test " + i);
            client.send(msg);
            eventHandler.addEvent(new TCPInputEvent(client));
            eventHandler.run();
            Assert.assertEquals(msg, server.receive().getPayload());
        }
    }

    @Test
    public void unorderedPacketsAreNotReceivedTest(){
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        Message msg = new Message( "test1");
        client.send(msg);
        eventHandler.addEvent(new TCPInputEvent(client));

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
        ClassicTCP client = new ClassicTCP();
        ClassicTCP server = new ClassicTCP();
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
        eventHandler.addEvent(new TCPConnectEvent(client, server));
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
            client.send(msg);
            eventHandler.addEvent(new TCPInputEvent(client));

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
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
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
        }
        eventHandler.run();

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
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
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
    public void packetIndexShouldUpdateAfterReceivingPacketInOrderTest() throws IllegalAccessException {
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        Packet packet = new PacketBuilder()
                .withConnection(client.getConnection())
                .withSequenceNumber(client.getConnection().getNextSequenceNumber())
                .build();


        int indexBeforeSending = server.getReceivingWindow().receivingPacketIndex(packet);
        Assert.assertEquals(0, indexBeforeSending);

        client.send(packet);
        eventHandler.addEvent(new TCPSendEvent(client));
        eventHandler.run();
        Assert.assertNotNull(server.receive());

        int indexAfterReceived = server.getReceivingWindow().receivingPacketIndex(packet);
        Assert.assertEquals(-1, indexAfterReceived);

    }

    @Test
    public void packetIndexShouldNotUpdateAfterReceivingPacketOutOfOrderButInWindowTest() throws IllegalAccessException {
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();


        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
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

        int indexBeforeSending = server.getReceivingWindow().receivingPacketIndex(packet);
        Assert.assertEquals(client.getWindowSize()-1, indexBeforeSending);


        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();
        Assert.assertNotNull(server.receive());

        int indexAfterReceived = server.getReceivingWindow().receivingPacketIndex(packet);
        Assert.assertEquals(client.getWindowSize()-1, indexAfterReceived);
    }

    @Test
    public void inWindowShouldWorkOnPacketsThatShouldBeInWindowTest() {
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        System.out.println("connected");

        Assert.assertNotNull(client.getConnection());
        Assert.assertNotNull(server.getConnection());

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();

        for (int i = 0; i < client.getWindowSize(); i++) {
            Packet packet = new PacketBuilder()
                    .withOrigin(client)
                    .withDestination(server)
                    .withSequenceNumber(seqNum + i)
                    .withAcknowledgmentNumber(ackNum + i)
                    .build();


            try{
                ReceivingWindow receivingWindow = server.getReceivingWindow();
                Assert.assertTrue(receivingWindow.inReceivingWindow(packet));
            }catch (IllegalAccessException e){
                Assert.fail();
            }
        }
    }

    @Test
    public void inWindowShouldNotWorkOnPacketsThatShouldNotBeInWindowTest() {
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
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

            try{
                ReceivingWindow receivingWindow = server.getReceivingWindow();
                Assert.assertFalse(receivingWindow.inReceivingWindow(packet));
            }catch (IllegalAccessException e){
                Assert.fail();
            }

        }
    }

    @Test
    public void floodWithPacketsInOrderShouldWorkTest(){
        ClassicTCP client = new ClassicTCP();
        Routable r1 = new Router.RouterBuilder().build();
        Routable r2 = new Router.RouterBuilder().build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(r1);
        r1.addChannel(r2);
        r2.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        int numPacketsToSend = server.getWindowSize() * 100;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));
        eventHandler.run();

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(r1.inputBufferIsEmpty());

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }
    }


    @Test
    public void floodWithPacketsInOrderButInLossyChannelShouldWorkTest() {
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().withNoiseTolerance(2).build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.isConnected());

        System.out.println("connected");

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(router.inputBufferIsEmpty());

        int numPacketsToSend = server.getWindowSize() * 100;
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        eventHandler.addEvent(new TCPInputEvent(client));
        eventHandler.run();

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        System.out.println(client.dequeueOutputBuffer());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(router.inputBufferIsEmpty());

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            System.out.println(received);
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }

        eventHandler.printStatistics();
    }

}
