package org.example.network;

import org.example.data.*;
import org.example.network.interfaces.Endpoint;

import org.example.network.interfaces.NetworkNode;
import org.example.simulator.EventHandler;
import org.example.simulator.events.RouteEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class RoutableTest {

    private static final Random RANDOM_GENERATOR = new Random();


    @Test
    public void routableWithRandomAddressAreNotEqualTest(){
        NetworkNode node1 = new Router.RouterBuilder().build();
        NetworkNode node2 = new Router.RouterBuilder().build();
        Assert.assertNotEquals(node1, node2);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationStraitLine(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(receivedPayload, msg);
    }


    @Test
    public  void routingPacketRoutsItToItsDestinationCircleGraph(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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


        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(receivedPayload, msg);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationWithCycle(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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


        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(receivedPayload,msg);
    }

    @Test
    public synchronized void routingPacketRoutsItToItsDestinationWithDeadEnd(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(msg, receivedPayload);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationForrest(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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


        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(msg, receivedPayload);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationWithUnconnectedNode(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(msg, receivedPayload);
    }


    @Test
    public synchronized void routingPacketRoutsItToItsDestinationCrazyGraph(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
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
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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

        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
        Payload receivedPayload = receivedPacket.getPayload();

        Assert.assertEquals(msg, receivedPayload);
    }



    @Test(expected = IllegalArgumentException.class)
    public synchronized void unconnectedClientCantRoutPacketToDestination(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

        r1.addChannel(r2);
        r2.addChannel(r3);
        server.addChannel(r3);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        Message msg = new Message( "hello på do!");
        client.route(new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build());
    }



    @Test(expected = IllegalArgumentException.class)
    public synchronized void unconnectedTreesCantRoutPacket(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

        client.addChannel(r1);
        r1.addChannel(r2);

        r3.addChannel(r4);
        server.addChannel(r4);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        Message msg = new Message( "hello på do!");
        client.route(new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build());
    }



    @Test
    public void faultyChannelsDropPacket(){
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().build();
        Router r2 = new Router.RouterBuilder().build();
        Router r3 = new Router.RouterBuilder().withNoiseTolerance(0).build();
        Router r4 = new Router.RouterBuilder().build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(100), new ArrayBlockingQueue<>(100), RANDOM_GENERATOR, 100);

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


        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder()
                .withPayload(msg)
                .withDestination(server)
                .build();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new RouteEvent(client, packet));
        eventHandler.run();

        Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();

        Assert.assertNull(null, receivedPacket);
    }


    @Test
    public void not100PercentLossyRoutersAreLoosingPacketIfEnoughPacketsAreSent(){
        double noiseTolerance = 2.5;
        Endpoint client = new RoutableEndpoint(new ArrayBlockingQueue<>(1000), new ArrayBlockingQueue<>(1000), RANDOM_GENERATOR, 100);
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).withBufferSize(1000).build();
        Router r2 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).withBufferSize(1000).build();
        Router r3 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).withBufferSize(1000).build();
        Router r4 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).withBufferSize(1000).build();
        Endpoint server = new RoutableEndpoint(new ArrayBlockingQueue<>(1000), new ArrayBlockingQueue<>(1000), RANDOM_GENERATOR, 100);

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

        for (int i = 0; i < 1000; i++) {
            Message msg = new Message( i + "");
            Packet packet = new PacketBuilder()
                    .withPayload(msg)
                    .withDestination(server)
                    .build();
            eventHandler.addEvent(new RouteEvent(client, packet));
        }

        eventHandler.run();

        for (int i = 0; i < 1000; i++) {
            Packet receivedPacket = ((RoutableEndpoint)server).getReceivedPacket();
            if (receivedPacket == null){
                return;
            }
        }
        Assert.fail();

    }


}
