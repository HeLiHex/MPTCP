package org.example.network;

import org.example.data.Flag;
import org.example.data.Message;
import org.example.data.Payload;
import org.example.network.interfaces.Endpoint;
import org.example.network.Router;
import org.example.data.Packet;

import org.example.protocol.BasicTCP;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class RoutableTest {

    private static final Random RANDOM_GENERATOR = new Random();


    public synchronized Payload getMsg(Endpoint server){
        for (int i = 0; i < 1000; i++) {
            Packet receivedPacket = server.dequeueInputBuffer();
            if (receivedPacket == null){
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            return receivedPacket.getPayload();
        }
        return null;
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationStraitLine(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

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

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);

        r1.interrupt();
        r2.interrupt();
        r3.interrupt();
        r4.interrupt();

        Assert.assertEquals(receivedPayload,msg);
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationCircleGraph(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        client.addChannel(r2);
        r1.addChannel(r3);
        r2.addChannel(r4);
        server.addChannel(r3);
        server.addChannel(r4);

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

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);

        r1.interrupt();
        r2.interrupt();
        r3.interrupt();
        r4.interrupt();

        Assert.assertEquals(receivedPayload,msg);
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationWithCycle(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);
        r1.addChannel(r3);
        r2.addChannel(r3);
        server.addChannel(r3);

        client.updateRoutingTable();
        server.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();

        r1.start();
        r2.start();
        r3.start();

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);

        r1.interrupt();
        r2.interrupt();
        r3.interrupt();

        Assert.assertEquals(receivedPayload,msg);
    }

    @Test
    public synchronized void routingPacketRoutsItToItsDestinationWithDeadEnd(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);
        r1.addChannel(r4);
        r2.addChannel(r3);
        server.addChannel(r4);

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

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);

        r1.interrupt();
        r2.interrupt();
        r3.interrupt();
        r4.interrupt();

        Assert.assertEquals(msg, receivedPayload);
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationForrest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        //tree one
        client.addChannel(r1);
        r1.addChannel(server);

        //tree two
        r2.addChannel(r3);
        r3.addChannel(r4);

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

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);

        r1.interrupt();
        r2.interrupt();
        r3.interrupt();
        r4.interrupt();

        Assert.assertEquals(msg, receivedPayload);
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationWithUnconnectedNode(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);
        r2.addChannel(r3);
        server.addChannel(r3);

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

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);

        r1.interrupt();
        r2.interrupt();
        r3.interrupt();
        r4.interrupt();

        Assert.assertEquals(msg, receivedPayload);
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationCrazyGraph(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        Router r5 = new Router(100, RANDOM_GENERATOR);
        Router r6 = new Router(100, RANDOM_GENERATOR);
        Router r7 = new Router(100, RANDOM_GENERATOR);
        Router r8 = new Router(100, RANDOM_GENERATOR);
        Router r9 = new Router(100, RANDOM_GENERATOR);
        Router r10 = new Router(100, RANDOM_GENERATOR);
        Router r11 = new Router(100, RANDOM_GENERATOR);
        Router r12 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        client.addChannel(r2);
        r1.addChannel(r3);
        r2.addChannel(r3);
        r3.addChannel(r4);
        r3.addChannel(r9);
        r4.addChannel(r5);
        r4.addChannel(r6);
        r5.addChannel(r6);
        r6.addChannel(r9);
        r6.addChannel(r7);
        r7.addChannel(r8);
        r9.addChannel(r10);
        r10.addChannel(r11);
        r11.addChannel(r12);
        r12.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        r5.updateRoutingTable();
        r6.updateRoutingTable();
        r7.updateRoutingTable();
        r8.updateRoutingTable();
        r9.updateRoutingTable();
        r10.updateRoutingTable();
        r11.updateRoutingTable();
        r12.updateRoutingTable();
        server.updateRoutingTable();

        r1.start();
        r2.start();
        r3.start();
        r4.start();
        r5.start();
        r6.start();
        r7.start();
        r8.start();
        r9.start();
        r10.start();
        r11.start();
        r12.start();

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);

        r1.interrupt();
        r2.interrupt();
        r3.interrupt();
        r4.interrupt();
        r5.interrupt();
        r6.interrupt();
        r7.interrupt();
        r8.interrupt();
        r9.interrupt();
        r10.interrupt();
        r11.interrupt();
        r12.interrupt();

        Assert.assertEquals(msg, receivedPayload);
    }



    @Test(expected = IllegalArgumentException.class)
    public synchronized void unconnectedClientCantRoutPacketToDestination(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        r1.addChannel(r2);
        r2.addChannel(r3);
        server.addChannel(r3);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        r1.start();
        r2.start();
        r3.start();

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());
    }



    @Test(expected = IllegalArgumentException.class)
    public synchronized void unconnectedTreesCantRoutPacket(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);

        r3.addChannel(r4);
        server.addChannel(r4);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        r1.start();
        r2.start();
        r3.start();
        r4.start();

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());
    }



    @Test
    public synchronized void faultyChannelsDropPacket(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR, 0);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);
        r2.addChannel(r3);
        r3.addChannel(r4);
        server.addChannel(r4);

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

        Message msg = new Message( "hello på do!");
        client.route(new Packet.PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());

        Payload receivedPayload = this.getMsg(server);
        Assert.assertEquals(null, receivedPayload);
    }


    @Test
    public synchronized void not100PercentLossyRoutersAreLoosingPacketIfEnoughPacketsAreSent(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        double noiseTolerance = 2.5;
        Router r1 = new Router(100, RANDOM_GENERATOR, noiseTolerance);
        Router r2 = new Router(100, RANDOM_GENERATOR, noiseTolerance);
        Router r3 = new Router(100, RANDOM_GENERATOR, noiseTolerance);
        Router r4 = new Router(100, RANDOM_GENERATOR,noiseTolerance);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);
        r2.addChannel(r3);
        r3.addChannel(r4);
        server.addChannel(r4);

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

        for (int i = 0; i < 1000; i++) {
            Message msg = new Message( "hello på do!");
            client.route(new Packet.PacketBuilder()
                    .withPayload(msg)
                    .withDestination(server)
                    .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                    .build());

            Payload receivedPayload = this.getMsg(server);
            if (receivedPayload == null){
                Assert.assertTrue(true);
                return;
            }
        }
        Assert.assertFalse(true);
    }


}
