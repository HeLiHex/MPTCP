package org.example.simulator;

import org.example.data.Message;
import org.example.network.Router;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.BasicTCP;
import org.example.protocol.TCP;
import org.example.simulator.events.ChannelEvent;
import org.example.simulator.events.Event;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.example.simulator.events.tcp.TCPRetransmitEventGenerator;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.Queue;

public class StatisticsTest {


    @Test
    public void noExceptionCallingToStringTest(){
        Statistics statistics = new Statistics();
        System.out.println(statistics.toString());
        Assert.assertTrue(statistics.toString().length() > 0);
    }

    private void connect(EventHandler eventHandler, TCP client, Endpoint endpoint){
        eventHandler.addEvent(new TCPConnectEvent(client, endpoint));
        eventHandler.run();
        Statistics.reset();
    }

    @Test
    public void statisticsAreConsistentNoLoss() {

        for (int j = 0; j < 10; j++) {
            EventHandler eventHandler = new EventHandler();

            BasicTCP client = new BasicTCP();
            BasicTCP server = new BasicTCP();
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
            Assert.assertEquals(0, Statistics.getNumberOfPacketsAckedMoreThanOnce());
            Assert.assertEquals(0, Statistics.getNumberOfPacketsRetransmitted());
        }
    }

    @Test
    public void statisticsAreConsistentWithLoss() {
        int numPacketsToSend = 400;
        for (int j = 0; j < 100; j++) {

            //first run
            EventHandler eventHandler = new EventHandler();
            Util.setSeed(1337);

            BasicTCP client = new BasicTCP();
            BasicTCP server = new BasicTCP();
            Router r1 = new Router.RouterBuilder()
                    .withNoiseTolerance(1)
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
            int numberOfPacketsAckedMoreThanOnce = Statistics.getNumberOfPacketsAckedMoreThanOnce();
            int numberOfPacketsRetransmitted = Statistics.getNumberOfPacketsRetransmitted();

            //run second time
            eventHandler = new EventHandler();
            Util.setSeed(1337);

            client = new BasicTCP();
            server = new BasicTCP();
            r1 = new Router.RouterBuilder()
                    .withNoiseTolerance(1)
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
            Assert.assertEquals(numPacketsToSend + Statistics.getNumberOfPacketsRetransmitted(), Statistics.getNumberOfPacketsSent());
            Assert.assertEquals(numberOfPacketsLost, Statistics.getNumberOfPacketsLost());
            Assert.assertEquals(numberOfPacketsDropped, Statistics.getNumberOfPacketsDropped());
            Assert.assertEquals(numberOfPacketsAckedMoreThanOnce, Statistics.getNumberOfPacketsAckedMoreThanOnce());
            Assert.assertEquals(numberOfPacketsRetransmitted, Statistics.getNumberOfPacketsRetransmitted());



        }
    }


    @Test
    public void statisticsAreAsExpectedInLossyChannelRunTest() {
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(2.5).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        connect(eventHandler, client, server);

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
        int packetsAckedMoreThanOnce = Statistics.getNumberOfPacketsAckedMoreThanOnce();

        Assert.assertEquals(
                numPacketsToSend + Statistics.getNumberOfPacketsRetransmitted(),
                Statistics.getNumberOfPacketsSent());
        System.out.println(packetsDropped);
        System.out.println(packetsAckedMoreThanOnce);
        System.out.println(packetsLost);
        System.out.println();
        System.out.println(packetsDropped + packetsLost + packetsAckedMoreThanOnce);
        System.out.println(Statistics.getNumberOfPacketsRetransmitted());
        Assert.assertEquals(0, Statistics.getNumberOfPacketsRetransmitted() - (packetsLost + packetsDropped + packetsAckedMoreThanOnce));

    }


    @Test
    public void connectStatistics() {
        EventHandler eventHandler = new EventHandler();
        Util.setSeed(1337);

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(1).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertEquals(1, Statistics.getNumberOfPacketsLost());
    }


    @Test
    public void TCPRetransmitEventGeneratorIsLastInEventQueueTest() {
        EventHandler eventHandler = new EventHandler();

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(1).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        connect(eventHandler, client, server);

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

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();
        Router r1 = new Router.RouterBuilder().withNoiseTolerance(1).build();

        client.addChannel(r1);
        r1.addChannel(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        server.updateRoutingTable();

        connect(eventHandler, client, server);

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

        BasicTCP client = new BasicTCP();
        BasicTCP server = new BasicTCP();

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
