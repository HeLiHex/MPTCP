package org.example.simulator;

import org.example.data.Message;
import org.example.network.Router;
import org.example.protocol.BasicTCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.TCPEvents.TCPConnectEvent;
import org.example.simulator.events.TCPEvents.TCPInputEvent;
import org.example.simulator.events.TCPEvents.TCPRetransmitEventGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Queue;
import java.util.Random;

public class StatisticsTest {


    @Test
    public void noExceptionCallingToStringTest(){
        Statistics statistics = new Statistics();
        System.out.println(statistics.toString());
    }

    @Test
    public void statisticsAreConsistentNoLoss() {

        for (int j = 0; j < 10; j++) {
            EventHandler eventHandler = new EventHandler();

            BasicTCP client = new BasicTCP(new Random(69));
            BasicTCP server = new BasicTCP(new Random(69));
            Router r1 = new Router.RouterBuilder().build();

            client.addChannel(r1);
            r1.addChannel(server);

            client.updateRoutingTable();
            r1.updateRoutingTable();
            server.updateRoutingTable();

            eventHandler.addEvent(new TCPConnectEvent(client, server));
            eventHandler.run();

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
            Assert.assertEquals(0, Statistics.getNumberOfPacketsAckedMoreThanOnce());
            Assert.assertEquals(0, Statistics.getNumberOfPacketsRetransmitted());
        }
    }

    @Test
    public void statisticsAreConsistentWithLoss() {
        Random random = new Random(1337);
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP(random);
        BasicTCP server = new BasicTCP(random);
        Router r1 = new Router.RouterBuilder()
                .withRandomGenerator(random)
                .withNoiseTolerance(1)
                .build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

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
        Assert.assertEquals(630, Statistics.getNumberOfPacketsLost());
        Assert.assertEquals(56, Statistics.getNumberOfPacketsDropped());
        Assert.assertEquals(125, Statistics.getNumberOfPacketsAckedMoreThanOnce());
        Assert.assertEquals(836, Statistics.getNumberOfPacketsRetransmitted());
    }


    @Test
    public void statisticsAreAsExpectedInLossyChannelRunTest() {
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP(new Random(69));
        BasicTCP server = new BasicTCP(new Random(69));
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(1).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        int multiplier = 100;
        int numPacketsToSend = server.getWindowSize() * multiplier;

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
        int packetsAckedMoreThanTwice = Statistics.getNumberOfPacketsAckedMoreThanOnce();

        Assert.assertEquals(numPacketsToSend + Statistics.getNumberOfPacketsRetransmitted(), Statistics.getNumberOfPacketsSent());
        Assert.assertEquals(0, Statistics.getNumberOfPacketsRetransmitted() - (packetsLost + packetsDropped + packetsAckedMoreThanTwice));

    }


    @Test
    public void TCPRetransmitEventGeneratorIsLastInEventQueueTest() {
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP(new Random(69));
        BasicTCP server = new BasicTCP(new Random(69));
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(1).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        int multiplier = 100;
        int numPacketsToSend = server.getWindowSize() * multiplier;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        while (!(eventHandler.peekEvent() instanceof TCPRetransmitEventGenerator)){
            eventHandler.singleRun();
        }

        Queue<Event> events = eventHandler.getEvents();
        while (!events.isEmpty()){
            Assert.assertEquals(events.poll().getClass(), TCPRetransmitEventGenerator.class);
        }
    }

    @Test
    public void TCPRetransmitEventGeneratorIsGeneratedPerPacketSentTest(){
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP(new Random(69));
        BasicTCP server = new BasicTCP(new Random(69));
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(1).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        int multiplier = 100;
        int numPacketsToSend = server.getWindowSize() * multiplier;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        while (!(eventHandler.peekEvent() instanceof TCPRetransmitEventGenerator)){
            eventHandler.singleRun();
        }

        Queue<Event> events = eventHandler.getEvents();
        Assert.assertEquals(Statistics.getNumberOfPackets(), events.size());
    }




    @Test
    public void trackNetworkNodeInputBufferTest() {
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP(new Random(1337));
        BasicTCP server = new BasicTCP(new Random(1337));

        //no loss
        Router r1 = new Router.RouterBuilder().build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        int numPacketsToSend = 1000;

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }
        eventHandler.addEvent(new TCPInputEvent(client));

        int channelEventCount = 0;
        while (eventHandler.peekEvent() != null){
            if (eventHandler.peekEvent() instanceof ChannelEvent){
                channelEventCount++;
            }
            eventHandler.singleRun();
        }
        int numberOfChannels = 4;
        Assert.assertEquals(numPacketsToSend * numberOfChannels, channelEventCount);



    }


}
