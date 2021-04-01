package org.example.simulator;

import org.example.data.Message;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.protocol.ClassicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.simulator.events.tcp.TCPRetransmitEventGenerator;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

public class StatisticsTest {

    @Rule
    public Timeout globalTimeout = new Timeout(50, TimeUnit.SECONDS);

    @Test
    public void noExceptionCallingToStringTest() {
        Statistics statistics = new Statistics();
        System.out.println(statistics.toString());
        Assert.assertTrue(statistics.toString().length() > 0);
    }

    private void connect(EventHandler eventHandler, TCP client, TCP endpoint) {
        eventHandler.addEvent(new TCPConnectEvent(client, endpoint));
        eventHandler.run();

        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(endpoint.isConnected());
        Statistics.reset();
    }

    @Test
    public void statisticsAreConsistentNoLoss() {
        for (int j = 0; j < 10; j++) {
            EventHandler eventHandler = new EventHandler();

            ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            Router r1 = new Router.RouterBuilder().build();

            client.addChannel(r1);
            r1.addChannel(server);

            client.updateRoutingTable();
            r1.updateRoutingTable();
            server.updateRoutingTable();

            connect(eventHandler, client, server);

            int numPacketsToSend = 400;

            for (int i = 1; i <= numPacketsToSend; i++) {
                Message msg = new Message("test " + i);
                client.send(msg);
            }
            eventHandler.addEvent(new TCPInputEvent(client));
            eventHandler.run();

            eventHandler.printStatistics();

            Assert.assertTrue(client.inputBufferIsEmpty());
            Assert.assertTrue(server.inputBufferIsEmpty());
            Assert.assertTrue(client.outputBufferIsEmpty());
            Assert.assertTrue(server.outputBufferIsEmpty());
            Assert.assertTrue(r1.inputBufferIsEmpty());

            Assert.assertEquals(numPacketsToSend, Statistics.getNumberOfPackets());
            Assert.assertEquals(numPacketsToSend, Statistics.getNumberOfPacketsReceived());
            Assert.assertEquals(numPacketsToSend + Statistics.getNumberOfPacketsRetransmitted(), Statistics.getNumberOfPacketsSent());
            Assert.assertEquals(0, Statistics.getNumberOfPacketsLost());
            Assert.assertEquals(0, Statistics.getNumberOfPacketsDropped());
            Assert.assertEquals(0, Statistics.getNumberOfPacketsFastRetransmitted());
            Assert.assertEquals(0, Statistics.getNumberOfPacketsRetransmitted());
        }
    }

    @Test
    public void statisticsAreConsistentWithLoss() {
        double noiseTolerance = 2.5;
        int numPacketsToSend = 1000;
        for (int j = 0; j < 50; j++) {

            //first run
            EventHandler eventHandler = new EventHandler();
            Util.setSeed(1337);

            ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            Router r1 = new Router.RouterBuilder()
                    .withNoiseTolerance(noiseTolerance)
                    .build();

            client.addChannel(r1);
            r1.addChannel(server);

            client.updateRoutingTable();
            r1.updateRoutingTable();
            server.updateRoutingTable();

            connect(eventHandler, client, server);

            for (int i = 1; i <= numPacketsToSend; i++) {
                Message msg = new Message("test " + i);
                client.send(msg);
            }
            eventHandler.addEvent(new TCPInputEvent(client));
            eventHandler.run();

            eventHandler.printStatistics();

            Assert.assertTrue(client.inputBufferIsEmpty());
            Assert.assertTrue(server.inputBufferIsEmpty());
            Assert.assertTrue(client.outputBufferIsEmpty());
            Assert.assertTrue(server.outputBufferIsEmpty());
            Assert.assertTrue(r1.inputBufferIsEmpty());


            int numberOfPacketsLost = Statistics.getNumberOfPacketsLost();
            int numberOfPacketsDropped = Statistics.getNumberOfPacketsDropped();
            int numberOfPacketsAckedMoreThanOnce = Statistics.getNumberOfPacketsFastRetransmitted();
            int numberOfPacketsRetransmitted = Statistics.getNumberOfPacketsRetransmitted();

            //run second time
            eventHandler = new EventHandler();
            Util.setSeed(1337);

            client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            r1 = new Router.RouterBuilder()
                    .withNoiseTolerance(noiseTolerance)
                    .build();

            client.addChannel(r1);
            r1.addChannel(server);

            client.updateRoutingTable();
            r1.updateRoutingTable();
            server.updateRoutingTable();

            connect(eventHandler, client, server);

            for (int i = 1; i <= numPacketsToSend; i++) {
                Message msg = new Message("test " + i);
                client.send(msg);
            }
            eventHandler.addEvent(new TCPInputEvent(client));
            eventHandler.run();

            eventHandler.printStatistics();

            Assert.assertTrue(client.inputBufferIsEmpty());
            Assert.assertTrue(server.inputBufferIsEmpty());
            Assert.assertTrue(client.outputBufferIsEmpty());
            Assert.assertTrue(server.outputBufferIsEmpty());
            Assert.assertTrue(r1.inputBufferIsEmpty());


            Assert.assertEquals(numPacketsToSend, Statistics.getNumberOfPackets());
            Assert.assertEquals(numPacketsToSend, Statistics.getNumberOfPacketsReceived());
            Assert.assertEquals(numPacketsToSend + Statistics.getNumberOfPacketsRetransmitted() + Statistics.getNumberOfPacketsFastRetransmitted(), Statistics.getNumberOfPacketsSent());
            Assert.assertEquals(numberOfPacketsLost, Statistics.getNumberOfPacketsLost());
            Assert.assertEquals(numberOfPacketsDropped, Statistics.getNumberOfPacketsDropped());
            Assert.assertEquals(numberOfPacketsAckedMoreThanOnce, Statistics.getNumberOfPacketsFastRetransmitted());
            Assert.assertEquals(numberOfPacketsRetransmitted, Statistics.getNumberOfPacketsRetransmitted());

        }
    }


    @Test
    public void statisticsAreAsExpectedInLossyChannelRunTest() {
        double noiseTolerance = 2.2;
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Routable r1 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        this.connect(eventHandler, client, server);

        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(server.isConnected());

        System.out.println("connected");

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(r1.inputBufferIsEmpty());

        int numPacketsToSend = server.getThisReceivingWindowCapacity() * 1000;
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        eventHandler.addEvent(new TCPInputEvent(client));
        eventHandler.run();

        eventHandler.printStatistics();

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(r1.inputBufferIsEmpty());

        Assert.assertEquals(numPacketsToSend, Statistics.getNumberOfPackets());
        Assert.assertEquals(numPacketsToSend, Statistics.getNumberOfPacketsReceived());

        Assert.assertTrue(0 < Statistics.getNumberOfPacketsLost());

        int packetsLost = Statistics.getNumberOfPacketsLost();
        int packetsDropped = Statistics.getNumberOfPacketsDropped();

        Assert.assertEquals(
                numPacketsToSend + Statistics.getNumberOfPacketsRetransmitted() + Statistics.getNumberOfPacketsFastRetransmitted(),
                Statistics.getNumberOfPacketsSent());

        int losses = (packetsLost + packetsDropped);
        int numRetransmitted = Statistics.getNumberOfPacketsRetransmitted() + Statistics.getNumberOfPacketsFastRetransmitted();
        System.out.println(numRetransmitted);
        System.out.println(losses);
        //Assert.assertTrue(numRetransmitted >= losses);
        Assert.assertTrue(Statistics.getNumberOfPacketsRetransmitted() <= losses * client.getThisReceivingWindowCapacity());
        //Assert.assertEquals(0, Statistics.getNumberOfPacketsRetransmitted() - (packetsLost + packetsDropped + packetsAckedMoreThanOnce));

    }


    @Test
    public void connectStatistics() {
        /*
        INFO
        This test may fail if a new call to random is added
         */
        EventHandler eventHandler = new EventHandler();
        Util.setSeed(1337);

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(1).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertEquals(0, Statistics.getNumberOfPacketsLost());
    }


    @Test
    public void TCPRetransmitEventGeneratorIsLastInEventQueueTest() {
        double noiseTolerance = 10000;
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        connect(eventHandler, client, server);

        int multiplier = 100;
        int numPacketsToSend = server.getThisReceivingWindowCapacity() * multiplier;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        Event lastEvent = null;
        while (!eventHandler.getEvents().isEmpty()){
            lastEvent = eventHandler.peekEvent();
            eventHandler.singleRun();
        }
        Assert.assertNotNull(lastEvent);
        Assert.assertEquals(TCPRetransmitEventGenerator.class, lastEvent.getClass());
    }

    @Test
    public void TCPRetransmitEventGeneratorIsGeneratedPerPacketSentTest() {
        double noiseTolerance = 2;
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(noiseTolerance).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        connect(eventHandler, client, server);

        int numPacketsToSend = 100;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        int numRetransmitGenerators = 0;

        while (eventHandler.peekEvent() != null){
            if (eventHandler.peekEvent() instanceof TCPRetransmitEventGenerator) numRetransmitGenerators++;
            eventHandler.singleRun();
        }

        Assert.assertEquals(Statistics.getNumberOfPacketsSent() - Statistics.getNumberOfPacketsFastRetransmitted(), numRetransmitGenerators);
    }


    @Test
    public void trackNetworkNodeInputBufferTest() {
        EventHandler eventHandler = new EventHandler();

        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        //no loss
        Router r1 = new Router.RouterBuilder().build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        connect(eventHandler, client, server);

        int numPacketsToSend = 1000;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        int channelEventCount = 0;
        while (eventHandler.peekEvent() != null) {
            if (eventHandler.peekEvent() instanceof ChannelEvent) {
                channelEventCount++;
            }
            eventHandler.singleRun();
        }
        int numberOfChannels = 4;
        //accumulative ack results in fewer ChannelEvents than numberOfChannels * numPacketsSent
        Assert.assertTrue(numPacketsToSend * numberOfChannels > channelEventCount);
        Assert.assertTrue(numPacketsToSend * (numberOfChannels/2) < channelEventCount);


    }


}
