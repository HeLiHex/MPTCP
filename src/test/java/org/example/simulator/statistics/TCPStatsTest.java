package org.example.simulator.statistics;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.network.address.SimpleAddress;
import org.example.protocol.MPTCP;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.RunTCPEvent;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.junit.Assert;
import org.junit.Test;

public class TCPStatsTest {

    @Test
    public void MPTCPFloodWithPacketsInOrderShouldWorkTest() {
        MPTCP client = new MPTCP(3, 21);
        Routable r1 = new Router.RouterBuilder().withNoiseTolerance(1000).withAddress(new SimpleAddress("A")).build();
        Routable r2 = new Router.RouterBuilder().withNoiseTolerance(1000).withAddress(new SimpleAddress("B")).build();
        Routable r3 = new Router.RouterBuilder().withNoiseTolerance(1000).withAddress(new SimpleAddress("C")).build();
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

        //receiver
        for (TCPStats stat: server.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createArrivalChart();
            stat.createDepartureChart();
            stat.createInterArrivalChart();
        }

        //sender
        for (TCPStats stat: client.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createCWNDChart();
        }

    }

}
