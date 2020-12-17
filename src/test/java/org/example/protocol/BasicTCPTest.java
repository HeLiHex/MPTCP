package org.example.protocol;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.network.Router;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static java.lang.Thread.sleep;

public class BasicTCPTest {


    private static final Random RANDOM_GENERATOR = new Random();

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

        Assert.assertEquals(server, client.getConnection().getConnectedNode());
        Assert.assertEquals(client, server.getConnection().getConnectedNode());

        Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());
    }

    @Test
    public void connectThenSendMsgWorksTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();

        client.connect(server);

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withConnection(client.getConnection())
                .withPayload(msg)
                .build();

        client.send(packet);


        Assert.assertEquals(getPacket(server), packet);

    }


    @Test
    public synchronized void connectThenSendMsgOverMultipleNodesLineWorksTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);

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

        r1.start();
        r2.start();
        r3.start();
        r4.start();
        server.start();

        client.connect(server);

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withConnection(client.getConnection())
                .withPayload(msg)
                .build();

        client.send(packet);

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(packet, getPacket(server));

    }


    @Test
    public void packetsAreOrderedTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

        for (int i = 0; i <= server.getWindowSize() * 2; i++) {
            Message msg = new Message( "test " + i);
            client.send(msg);

            //todo - the delay is here because lost packets are not retransmitted
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Assert.assertEquals(getPacket(server).getPayload(), msg);
        }
    }

    @Test
    public synchronized void unorderedPacketsAreNotReceivedTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

        Message msg = new Message( "test1");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withOrigin(client)
                .withDestination(server)
                .withSequenceNumber(client.getConnection().getNextSequenceNumber())
                .build();
        client.send(packet);

        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        msg = new Message( "test2");
        packet = new PacketBuilder()
                .withPayload(msg)
                .withOrigin(client)
                .withDestination(server)
                .withSequenceNumber(client.getConnection().getNextSequenceNumber() + 100)
                .build();
        client.route(packet);

        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertNotEquals(getPacket(server).getPayload(), msg);
    }



    @Test
    public synchronized void unorderedPacketsAreDroppedAndOrderedPacketsAreReceivedWithoutBlockTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);

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

        r1.start();
        r2.start();
        r3.start();
        r4.start();
        server.start();

        client.connect(server);
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < server.getWindowSize(); i++) {
            Message msg = new Message( "test " + i);
            client.route(new PacketBuilder()
                    .withSequenceNumber(5000 + i)
                    .withAcknowledgmentNumber(20000 + i)
                    .withOrigin(client)
                    .withDestination(server)
                    .withPayload(new Message("should not be received 1"))
                    .build()
            );

            client.send(msg);

            client.route(new PacketBuilder()
                    .withSequenceNumber(100 + i)
                    .withAcknowledgmentNumber(20 + i)
                    .withOrigin(client)
                    .withDestination(server)
                    .withPayload(new Message("should not be received 2"))
                    .build()
            );

            //todo - the delay is here because lost packets are not retransmitted
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        for (int i = 0; i < server.getWindowSize(); i++) {
            Packet received = getPacket(server);
            Assert.assertNotNull(received);
            Assert.assertEquals("test " + i, received.getPayload().toString());
        }

        Packet received = getPacket(server);
        Assert.assertNull(received);
    }


    @Test
    public void routedMessagesUnorderedReceiveOrderedTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();

        client.connect(server);
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();


        for (int i = client.getWindowSize() - 1; i >= 0 ; i--) {
            client.route(new PacketBuilder()
                    .withOrigin(client)
                    .withDestination(server)
                    .withSequenceNumber(seqNum + i)
                    .withAcknowledgmentNumber(ackNum + i)
                    .withPayload(new Message(i + ""))
                    .build()
            );
        }

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < client.getWindowSize(); i++) {
            Packet received = getPacket(server);
            Assert.assertNotNull("iteration: " + i, received);
            Assert.assertEquals(i + "", received.getPayload().toString());
        }

        Packet received = getPacket(server);
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
                sleep(10);
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

        int numPacketsToSend = server.getWindowSize() * 2;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            client.send(msg);

            //todo - the delay is here because lost packets are not retransmitted
            /*try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }

        /*try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Assert.assertEquals(getPacket(server).getPayload(), msg);
        }

    }


    @Test
    public void floodWithPacketsInOrderButInLossyChannelShouldWorkTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR, 100);
        Router r2 = new Router(100, RANDOM_GENERATOR, 100);
        Router r3 = new Router(100, RANDOM_GENERATOR, 2);
        Router r4 = new Router(100, RANDOM_GENERATOR, 100);

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

        r1.start();
        r2.start();
        r3.start();
        r4.start();
        server.start();

        client.connect(server);

        int numPacketsToSend = server.getWindowSize() * 2;

        for (int i = 1; i < numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            client.send(msg);

        }

        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = getPacket(server);
            Assert.assertNotNull(received);
            Assert.assertEquals(received.getPayload(), msg);
        }

    }





}
