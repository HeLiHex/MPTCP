package org.example.network;

import org.example.data.*;
import org.example.network.interfaces.Endpoint;
import org.example.network.Router;

import org.example.network.interfaces.NetworkNode;
import org.example.protocol.BasicTCP;
import org.example.simulator.EventHandler;
import org.example.simulator.events.RouteEvent;
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
    public void routablesWithRandomAddressAreNotEqualTest(){
        NetworkNode node1 = new Router.RouterBuilder().build();
        NetworkNode node2 = new Router.RouterBuilder().build();
        Assert.assertNotEquals(node1, node2);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationStraitLine(){
        Endpoint client = new RoutableEndpoint(new BufferQueue<>(100), new BufferQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new BufferQueue<>(100), new BufferQueue<>(100), RANDOM_GENERATOR, 100);

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
                //.withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(receivedPayload, msg);
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationCircleGraph(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
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
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
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
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
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
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
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
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
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
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Router r5 = new Router.RouterBuilder().build();
        Router r6 = new Router.RouterBuilder().build();
        Router r7 = new Router.RouterBuilder().build();
        Router r8 = new Router.RouterBuilder().build();
        Router r9 = new Router.RouterBuilder().build();
        Router r10 = new Router.RouterBuilder().build();
        Router r11 = new Router.RouterBuilder().build();
        Router r12 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
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
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();;
        Router r3 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());
    }



    @Test(expected = IllegalArgumentException.class)
    public synchronized void unconnectedTreesCantRoutPacket(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build());
    }



    @Test
    public synchronized void faultyChannelsDropPacket(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().withNoiseTolerance(0).build();
        Router r4 = new Router.RouterBuilder().build();
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
        client.route(new PacketBuilder()
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
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).build();
        Router r2 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).build();
        Router r3 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).build();
        Router r4 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).build();
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
            client.route(new PacketBuilder()
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
