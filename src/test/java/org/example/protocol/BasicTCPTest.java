package org.example.protocol;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Router;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
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
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(server, client.getConnection().getConnectedNode());
        Assert.assertEquals(client, server.getConnection().getConnectedNode());

        Assert.assertEquals(server.getConnection().getNextSequenceNumber() , client.getConnection().getNextAcknowledgementNumber());
    }

    @Test
    public synchronized void connectThenSendMsgWorksTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();

        client.connect(server);

        Message msg = new Message( "hello på do!");
        Packet packet = new Packet.PacketBuilder()
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

        Message msg = new Message( "hello på do!");
        Packet packet = new Packet.PacketBuilder()
                .withConnection(client.getConnection())
                .withPayload(msg)
                .build();

        client.send(packet);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(getPacket(server), packet);

    }


    @Test
    public synchronized void packetsAreOrderedTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();
        client.connect(server);

        for (int i = 0; i < 10; i++) {
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
        Packet packet = new Packet.PacketBuilder()
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
        packet = new Packet.PacketBuilder()
                .withPayload(msg)
                .withOrigin(client)
                .withDestination(server)
                .withSequenceNumber(client.getConnection().getNextSequenceNumber() + 100)
                .build();
        client.send(packet);

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

        for (int i = 0; i < 4; i++) {
            Message msg = new Message( "test " + i);
            client.send(new Packet.PacketBuilder()
                    .withSequenceNumber(5000 + i)
                    .withAcknowledgmentNumber(20000 + i)
                    .withOrigin(client)
                    .withDestination(server)
                    .withPayload(new Message("should not be received"))
                    .build()
            );

            client.send(msg);

            client.route(new Packet.PacketBuilder()
                    .withSequenceNumber(100 + i)
                    .withAcknowledgmentNumber(20 + i)
                    .withOrigin(client)
                    .withDestination(server)
                    .withPayload(new Message("should not be received"))
                    .build()
            );

            //todo - the delay is here because lost packets are not retransmitted
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        Packet received;

        received = getPacket(server);
        Assert.assertNotNull(received);
        Assert.assertEquals("test 0", received.getPayload().toString());

        received = getPacket(server);
        Assert.assertNotNull(received);
        Assert.assertEquals("test 1", received.getPayload().toString());

        received = getPacket(server);
        Assert.assertNotNull(received);
        Assert.assertEquals("test 2", received.getPayload().toString());

        received = getPacket(server);
        Assert.assertNotNull(received);
        Assert.assertEquals("test 3", received.getPayload().toString());

        received = getPacket(server);
        Assert.assertNull(received);
    }


    @Test
    public synchronized void sendMessagesUnorderedReceiveOrderedTest() {
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        client.updateRoutingTable();
        server.updateRoutingTable();

        server.start();

        client.connect(server);

        int seqNum = client.getConnection().getNextSequenceNumber();
        int ackNum = client.getConnection().getNextAcknowledgementNumber();


        for (int i = client.getWindowSize() - 1; i >= 0 ; i--) {
            client.send(new Packet.PacketBuilder()
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
    public synchronized void sendToManyMessagesUnorderedReceiveOrderedAndDropCorrectTest() {
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
            client.send(new Packet.PacketBuilder()
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
            Assert.assertNotNull(received);
            Assert.assertEquals(i + "", received.getPayload().toString());
        }

        Packet received = getPacket(server);
        Assert.assertNull(received);

    }



}
