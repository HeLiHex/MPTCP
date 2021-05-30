import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.network.address.SimpleAddress;
import org.example.protocol.ClassicTCP;
import org.example.protocol.MPTCP;
import org.example.protocol.TCP;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.statistics.TCPStats;
import org.example.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Simulator {

    private int numPacketsToSend = 2000;

    @Before
    public void setUp() {
        Util.resetTime();
        Util.setSeed(1);
    }

    private void numPacketsToSend(TCP tcpToSend, int numPacketsToSend) {
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            tcpToSend.send(msg);
        }
    }

    private EventHandler connectAndRun(TCP client, TCP server) {
        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        return eventHandler;
    }

    private void allReceived(TCP receiver, int numPacketsToSend) {
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            Packet received = receiver.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }
    }

    @Test
    public void shortPathLowLoss() {
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-ShortPathLowLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-ShortPathLowLoss")).build();

        new Channel.ChannelBuilder().withCost(1).build(client, r1);
        new Channel.ChannelBuilder().withCost(10).build(r1, r2);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.001).build(r2, r3);
        new Channel.ChannelBuilder().withCost(10).build(r3, r4);
        new Channel.ChannelBuilder().withCost(1).build(r4, server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(client, server);

        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        TCPStats stat = client.getStats();
        System.out.println(stat);
        stat.createArrivalChart();
        stat.createDepartureChart();
        stat.createInterArrivalChart();
        stat.createTimeInSystemChart();
        stat.createNumberOfPacketsInSystemChart();

        System.out.println(server.getStats().toString());
        server.getStats().createCWNDChart();
        server.getStats().createDepartureChart();
    }

    @Test
    public void shortPathHighLoss() {
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-ShortPathHighLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-ShortPathHighLoss")).build();


        new Channel.ChannelBuilder().withCost(1).build(client, r1);
        new Channel.ChannelBuilder().withCost(10).build(r1, r2);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.01).build(r2, r3);
        new Channel.ChannelBuilder().withCost(10).build(r3, r4);
        new Channel.ChannelBuilder().withCost(1).build(r4, server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(client, server);

        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        TCPStats stat = client.getStats();
        System.out.println(stat);
        stat.createArrivalChart();
        stat.createDepartureChart();
        stat.createInterArrivalChart();
        stat.createTimeInSystemChart();
        stat.createNumberOfPacketsInSystemChart();

        System.out.println(server.getStats().toString());
        server.getStats().createCWNDChart();
        server.getStats().createDepartureChart();
    }

    @Test
    public void longPathLowLoss() {
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-LongPathLowLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 4")).build();
        Routable r5 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 5")).build();
        Routable r6 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 6")).build();
        Routable r7 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 7")).build();
        Routable r8 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 8")).build();
        Routable r9 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 9")).build();
        Routable r10 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 10")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-LongPathLowLoss")).build();

        new Channel.ChannelBuilder().withCost(1).build(client, r1);
        new Channel.ChannelBuilder().withCost(10).build(r1, r2);
        new Channel.ChannelBuilder().withCost(50).build(r2, r3);
        new Channel.ChannelBuilder().withCost(50).build(r3, r4);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r4, r5);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r5, r6);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r6, r7);
        new Channel.ChannelBuilder().withCost(50).build(r7, r8);
        new Channel.ChannelBuilder().withCost(50).build(r8, r9);
        new Channel.ChannelBuilder().withCost(10).build(r9, r10);
        new Channel.ChannelBuilder().withCost(1).build(r10, server);

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
        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(client, server);

        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        TCPStats stat = client.getStats();
        System.out.println(stat);
        stat.createArrivalChart();
        stat.createDepartureChart();
        stat.createInterArrivalChart();
        stat.createTimeInSystemChart();
        stat.createNumberOfPacketsInSystemChart();

        System.out.println(server.getStats().toString());
        server.getStats().createCWNDChart();
    }

    @Test
    public void longPathHighLoss() {
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-LongPathHighLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 4")).build();
        Routable r5 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 5")).build();
        Routable r6 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 6")).build();
        Routable r7 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 7")).build();
        Routable r8 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 8")).build();
        Routable r9 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 9")).build();
        Routable r10 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 10")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-LongPathHighLoss")).build();

        new Channel.ChannelBuilder().withCost(1).build(client, r1);
        new Channel.ChannelBuilder().withCost(10).build(r1, r2);
        new Channel.ChannelBuilder().withCost(50).build(r2, r3);
        new Channel.ChannelBuilder().withCost(50).build(r3, r4);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r4, r5);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.01).build(r5, r6);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r6, r7);
        new Channel.ChannelBuilder().withCost(50).build(r7, r8);
        new Channel.ChannelBuilder().withCost(50).build(r8, r9);
        new Channel.ChannelBuilder().withCost(10).build(r9, r10);
        new Channel.ChannelBuilder().withCost(1).build(r10, server);

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
        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(client, server);

        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        TCPStats stat = client.getStats();
        System.out.println(stat);
        stat.createArrivalChart();
        stat.createDepartureChart();
        stat.createInterArrivalChart();
        stat.createTimeInSystemChart();
        stat.createNumberOfPacketsInSystemChart();

        System.out.println(server.getStats().toString());
        server.getStats().createCWNDChart();
    }


    private void getStats(MPTCP sender, MPTCP receiver) {
        //receiver
        for (TCPStats stat : receiver.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createArrivalChart();
            stat.createDepartureChart();
            stat.createInterArrivalChart();
            stat.createTimeInSystemChart();
            stat.createNumberOfPacketsInSystemChart();
        }

        //sender
        for (TCPStats stat : sender.getTcpStats()) {
            System.out.println(stat.toString());
            stat.createCWNDChart();
        }
    }

    @Test
    public void MPTCP_HomoShortPathLowLoss() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Client-ShortPathLowLoss")).build();

        Routable r11 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r12 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r13 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r14 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();

        Routable r21 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r22 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r23 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r24 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Server-ShortPathLowLoss")).build();

        //path one
        new Channel.ChannelBuilder().withCost(1).build(client, r11);
        new Channel.ChannelBuilder().withCost(10).build(r11, r12);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.001).build(r12, r13);
        new Channel.ChannelBuilder().withCost(10).build(r13, r14);
        new Channel.ChannelBuilder().withCost(1).build(r14, server);

        //path two
        new Channel.ChannelBuilder().withCost(1).build(client, r21);
        new Channel.ChannelBuilder().withCost(10).build(r21, r22);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.001).build(r22, r23);
        new Channel.ChannelBuilder().withCost(10).build(r23, r24);
        new Channel.ChannelBuilder().withCost(1).build(r24, server);

        client.updateRoutingTable();
        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r13.updateRoutingTable();
        r14.updateRoutingTable();
        r21.updateRoutingTable();
        r22.updateRoutingTable();
        r23.updateRoutingTable();
        r24.updateRoutingTable();
        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(server, client);

        Assert.assertTrue("client still has packets to send", client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r11.inputBufferIsEmpty());
        Assert.assertTrue(r12.inputBufferIsEmpty());
        Assert.assertTrue(r13.inputBufferIsEmpty());
        Assert.assertTrue(r14.inputBufferIsEmpty());
        Assert.assertTrue(r21.inputBufferIsEmpty());
        Assert.assertTrue(r22.inputBufferIsEmpty());
        Assert.assertTrue(r23.inputBufferIsEmpty());
        Assert.assertTrue(r24.inputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        Assert.assertNull(server.receive());
        this.getStats(server, client);

    }

    @Test
    public void MPTCP_HomoShortPathHighLoss() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Client-ShortPathHighLoss")).build();

        Routable r11 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r12 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r13 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r14 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();

        Routable r21 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r22 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r23 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r24 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Server-ShortPathHighLoss")).build();

        //path one
        new Channel.ChannelBuilder().withCost(1).build(client, r11);
        new Channel.ChannelBuilder().withCost(10).build(r11, r12);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.01).build(r12, r13);
        new Channel.ChannelBuilder().withCost(10).build(r13, r14);
        new Channel.ChannelBuilder().withCost(1).build(r14, server);

        //path two
        new Channel.ChannelBuilder().withCost(1).build(client, r21);
        new Channel.ChannelBuilder().withCost(10).build(r21, r22);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.01).build(r22, r23);
        new Channel.ChannelBuilder().withCost(10).build(r23, r24);
        new Channel.ChannelBuilder().withCost(1).build(r24, server);

        client.updateRoutingTable();
        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r13.updateRoutingTable();
        r14.updateRoutingTable();
        r21.updateRoutingTable();
        r22.updateRoutingTable();
        r23.updateRoutingTable();
        r24.updateRoutingTable();
        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(server, client);

        Assert.assertTrue("client still has packets to send", client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r11.inputBufferIsEmpty());
        Assert.assertTrue(r12.inputBufferIsEmpty());
        Assert.assertTrue(r13.inputBufferIsEmpty());
        Assert.assertTrue(r14.inputBufferIsEmpty());
        Assert.assertTrue(r21.inputBufferIsEmpty());
        Assert.assertTrue(r22.inputBufferIsEmpty());
        Assert.assertTrue(r23.inputBufferIsEmpty());
        Assert.assertTrue(r24.inputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        Assert.assertNull(server.receive());
        this.getStats(server, client);

    }

    @Test
    public void MPTCP_HomoLongPathLowLoss() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Client-LongPathLowLoss")).build();

        Routable r11 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 11")).build();
        Routable r12 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 12")).build();
        Routable r13 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 13")).build();
        Routable r14 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 14")).build();
        Routable r15 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 15")).build();
        Routable r16 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 16")).build();
        Routable r17 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 17")).build();
        Routable r18 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 18")).build();
        Routable r19 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 19")).build();
        Routable r110 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 110")).build();

        Routable r21 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 21")).build();
        Routable r22 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 22")).build();
        Routable r23 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 23")).build();
        Routable r24 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 24")).build();
        Routable r25 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 25")).build();
        Routable r26 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 26")).build();
        Routable r27 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 27")).build();
        Routable r28 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 28")).build();
        Routable r29 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 29")).build();
        Routable r210 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 210")).build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Server-LongPathLowLoss")).build();

        //path one
        new Channel.ChannelBuilder().withCost(1).build(client, r11);
        new Channel.ChannelBuilder().withCost(10).build(r11, r12);
        new Channel.ChannelBuilder().withCost(50).build(r12, r13);
        new Channel.ChannelBuilder().withCost(50).build(r13, r14);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r14, r15);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r15, r16);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r16, r17);
        new Channel.ChannelBuilder().withCost(50).build(r17, r18);
        new Channel.ChannelBuilder().withCost(50).build(r18, r19);
        new Channel.ChannelBuilder().withCost(10).build(r19, r110);
        new Channel.ChannelBuilder().withCost(1).build(r110, server);

        //path two
        new Channel.ChannelBuilder().withCost(1).build(client, r21);
        new Channel.ChannelBuilder().withCost(10).build(r21, r22);
        new Channel.ChannelBuilder().withCost(50).build(r22, r23);
        new Channel.ChannelBuilder().withCost(50).build(r23, r24);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r24, r25);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r25, r26);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r26, r27);
        new Channel.ChannelBuilder().withCost(50).build(r27, r28);
        new Channel.ChannelBuilder().withCost(50).build(r28, r29);
        new Channel.ChannelBuilder().withCost(10).build(r29, r210);
        new Channel.ChannelBuilder().withCost(1).build(r210, server);

        client.updateRoutingTable();

        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r13.updateRoutingTable();
        r14.updateRoutingTable();
        r15.updateRoutingTable();
        r16.updateRoutingTable();
        r17.updateRoutingTable();
        r18.updateRoutingTable();
        r19.updateRoutingTable();
        r110.updateRoutingTable();

        r21.updateRoutingTable();
        r22.updateRoutingTable();
        r23.updateRoutingTable();
        r24.updateRoutingTable();
        r25.updateRoutingTable();
        r26.updateRoutingTable();
        r27.updateRoutingTable();
        r28.updateRoutingTable();
        r29.updateRoutingTable();
        r210.updateRoutingTable();

        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(server, client);

        Assert.assertTrue("client still has packets to send", client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r11.inputBufferIsEmpty());
        Assert.assertTrue(r12.inputBufferIsEmpty());
        Assert.assertTrue(r13.inputBufferIsEmpty());
        Assert.assertTrue(r14.inputBufferIsEmpty());
        Assert.assertTrue(r15.inputBufferIsEmpty());
        Assert.assertTrue(r16.inputBufferIsEmpty());
        Assert.assertTrue(r17.inputBufferIsEmpty());
        Assert.assertTrue(r18.inputBufferIsEmpty());
        Assert.assertTrue(r19.inputBufferIsEmpty());
        Assert.assertTrue(r110.inputBufferIsEmpty());

        Assert.assertTrue(r21.inputBufferIsEmpty());
        Assert.assertTrue(r22.inputBufferIsEmpty());
        Assert.assertTrue(r23.inputBufferIsEmpty());
        Assert.assertTrue(r24.inputBufferIsEmpty());
        Assert.assertTrue(r25.inputBufferIsEmpty());
        Assert.assertTrue(r26.inputBufferIsEmpty());
        Assert.assertTrue(r27.inputBufferIsEmpty());
        Assert.assertTrue(r28.inputBufferIsEmpty());
        Assert.assertTrue(r29.inputBufferIsEmpty());
        Assert.assertTrue(r210.inputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        Assert.assertNull(server.receive());
        this.getStats(server, client);

    }

    @Test
    public void MPTCP_HomoLongPathHighLoss() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Client-LongPathHighLoss")).build();

        Routable r11 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 11")).build();
        Routable r12 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 12")).build();
        Routable r13 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 13")).build();
        Routable r14 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 14")).build();
        Routable r15 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 15")).build();
        Routable r16 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 16")).build();
        Routable r17 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 17")).build();
        Routable r18 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 18")).build();
        Routable r19 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 19")).build();
        Routable r110 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 110")).build();

        Routable r21 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 21")).build();
        Routable r22 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 22")).build();
        Routable r23 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 23")).build();
        Routable r24 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 24")).build();
        Routable r25 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 25")).build();
        Routable r26 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 26")).build();
        Routable r27 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 27")).build();
        Routable r28 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 28")).build();
        Routable r29 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 29")).build();
        Routable r210 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 210")).build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Server-LongPathHighLoss")).build();

        //path one
        new Channel.ChannelBuilder().withCost(1).build(client, r11);
        new Channel.ChannelBuilder().withCost(10).build(r11, r12);
        new Channel.ChannelBuilder().withCost(50).build(r12, r13);
        new Channel.ChannelBuilder().withCost(50).build(r13, r14);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r14, r15);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.01).build(r15, r16);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r16, r17);
        new Channel.ChannelBuilder().withCost(50).build(r17, r18);
        new Channel.ChannelBuilder().withCost(50).build(r18, r19);
        new Channel.ChannelBuilder().withCost(10).build(r19, r110);
        new Channel.ChannelBuilder().withCost(1).build(r110, server);

        //path two
        new Channel.ChannelBuilder().withCost(1).build(client, r21);
        new Channel.ChannelBuilder().withCost(10).build(r21, r22);
        new Channel.ChannelBuilder().withCost(50).build(r22, r23);
        new Channel.ChannelBuilder().withCost(50).build(r23, r24);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r24, r25);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.01).build(r25, r26);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r26, r27);
        new Channel.ChannelBuilder().withCost(50).build(r27, r28);
        new Channel.ChannelBuilder().withCost(50).build(r28, r29);
        new Channel.ChannelBuilder().withCost(10).build(r29, r210);
        new Channel.ChannelBuilder().withCost(1).build(r210, server);

        client.updateRoutingTable();

        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r13.updateRoutingTable();
        r14.updateRoutingTable();
        r15.updateRoutingTable();
        r16.updateRoutingTable();
        r17.updateRoutingTable();
        r18.updateRoutingTable();
        r19.updateRoutingTable();
        r110.updateRoutingTable();

        r21.updateRoutingTable();
        r22.updateRoutingTable();
        r23.updateRoutingTable();
        r24.updateRoutingTable();
        r25.updateRoutingTable();
        r26.updateRoutingTable();
        r27.updateRoutingTable();
        r28.updateRoutingTable();
        r29.updateRoutingTable();
        r210.updateRoutingTable();

        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(server, client);

        Assert.assertTrue("client still has packets to send", client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r11.inputBufferIsEmpty());
        Assert.assertTrue(r12.inputBufferIsEmpty());
        Assert.assertTrue(r13.inputBufferIsEmpty());
        Assert.assertTrue(r14.inputBufferIsEmpty());
        Assert.assertTrue(r15.inputBufferIsEmpty());
        Assert.assertTrue(r16.inputBufferIsEmpty());
        Assert.assertTrue(r17.inputBufferIsEmpty());
        Assert.assertTrue(r18.inputBufferIsEmpty());
        Assert.assertTrue(r19.inputBufferIsEmpty());
        Assert.assertTrue(r110.inputBufferIsEmpty());

        Assert.assertTrue(r21.inputBufferIsEmpty());
        Assert.assertTrue(r22.inputBufferIsEmpty());
        Assert.assertTrue(r23.inputBufferIsEmpty());
        Assert.assertTrue(r24.inputBufferIsEmpty());
        Assert.assertTrue(r25.inputBufferIsEmpty());
        Assert.assertTrue(r26.inputBufferIsEmpty());
        Assert.assertTrue(r27.inputBufferIsEmpty());
        Assert.assertTrue(r28.inputBufferIsEmpty());
        Assert.assertTrue(r29.inputBufferIsEmpty());
        Assert.assertTrue(r210.inputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        Assert.assertNull(server.receive());
        this.getStats(server, client);

    }

    @Test
    public void MPTCP_HeteroLowLoss() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Client-HeteroLowLoss")).build();

        Routable r11 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 11")).build();
        Routable r12 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 12")).build();
        Routable r13 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 13")).build();
        Routable r14 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 14")).build();
        Routable r15 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 15")).build();
        Routable r16 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 16")).build();
        Routable r17 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 17")).build();
        Routable r18 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 18")).build();
        Routable r19 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 19")).build();
        Routable r110 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 110")).build();

        Routable r21 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r22 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r23 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r24 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Server-HeteroLowLoss")).build();

        // long path
        new Channel.ChannelBuilder().withCost(1).build(client, r11);
        new Channel.ChannelBuilder().withCost(10).build(r11, r12);
        new Channel.ChannelBuilder().withCost(50).build(r12, r13);
        new Channel.ChannelBuilder().withCost(50).build(r13, r14);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r14, r15);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r15, r16);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r16, r17);
        new Channel.ChannelBuilder().withCost(50).build(r17, r18);
        new Channel.ChannelBuilder().withCost(50).build(r18, r19);
        new Channel.ChannelBuilder().withCost(10).build(r19, r110);
        new Channel.ChannelBuilder().withCost(1).build(r110, server);

        // short path
        new Channel.ChannelBuilder().withCost(1).build(client, r21);
        new Channel.ChannelBuilder().withCost(10).build(r21, r22);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.001).build(r22, r23);
        new Channel.ChannelBuilder().withCost(10).build(r23, r24);
        new Channel.ChannelBuilder().withCost(1).build(r24, server);

        client.updateRoutingTable();

        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r13.updateRoutingTable();
        r14.updateRoutingTable();
        r15.updateRoutingTable();
        r16.updateRoutingTable();
        r17.updateRoutingTable();
        r18.updateRoutingTable();
        r19.updateRoutingTable();
        r110.updateRoutingTable();

        r21.updateRoutingTable();
        r22.updateRoutingTable();
        r23.updateRoutingTable();
        r24.updateRoutingTable();

        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(server, client);


        Assert.assertTrue("client still has packets to send", client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r11.inputBufferIsEmpty());
        Assert.assertTrue(r12.inputBufferIsEmpty());
        Assert.assertTrue(r13.inputBufferIsEmpty());
        Assert.assertTrue(r14.inputBufferIsEmpty());
        Assert.assertTrue(r15.inputBufferIsEmpty());
        Assert.assertTrue(r16.inputBufferIsEmpty());
        Assert.assertTrue(r17.inputBufferIsEmpty());
        Assert.assertTrue(r18.inputBufferIsEmpty());
        Assert.assertTrue(r19.inputBufferIsEmpty());
        Assert.assertTrue(r110.inputBufferIsEmpty());

        Assert.assertTrue(r21.inputBufferIsEmpty());
        Assert.assertTrue(r22.inputBufferIsEmpty());
        Assert.assertTrue(r23.inputBufferIsEmpty());
        Assert.assertTrue(r24.inputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        Assert.assertNull(server.receive());
        this.getStats(server, client);

    }

    @Test
    public void MPTCP_HeteroHighLoss() {
        MPTCP client = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Client-HeteroHighLoss")).build();

        Routable r11 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 11")).build();
        Routable r12 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 12")).build();
        Routable r13 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 13")).build();
        Routable r14 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 14")).build();
        Routable r15 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 15")).build();
        Routable r16 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 16")).build();
        Routable r17 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 17")).build();
        Routable r18 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 18")).build();
        Routable r19 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 19")).build();
        Routable r110 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 110")).build();

        Routable r21 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 1")).build();
        Routable r22 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r23 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r24 = new Router.RouterBuilder().withAverageQueueUtilization(0.5).withAddress(new SimpleAddress("Router 4")).build();

        MPTCP server = new MPTCP.MPTCPBuilder().withNumberOfSubflows(2).withReceivingWindowCapacity(30).withAddress(new SimpleAddress("MPTCP-Server-HeteroHighLoss")).build();

        // long path
        new Channel.ChannelBuilder().withCost(1).build(client, r11);
        new Channel.ChannelBuilder().withCost(10).build(r11, r12);
        new Channel.ChannelBuilder().withCost(50).build(r12, r13);
        new Channel.ChannelBuilder().withCost(50).build(r13, r14);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r14, r15);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.01).build(r15, r16);
        new Channel.ChannelBuilder().withCost(50).withLoss(0.001).build(r16, r17);
        new Channel.ChannelBuilder().withCost(50).build(r17, r18);
        new Channel.ChannelBuilder().withCost(50).build(r18, r19);
        new Channel.ChannelBuilder().withCost(10).build(r19, r110);
        new Channel.ChannelBuilder().withCost(1).build(r110, server);

        // short path
        new Channel.ChannelBuilder().withCost(1).build(client, r21);
        new Channel.ChannelBuilder().withCost(10).build(r21, r22);
        new Channel.ChannelBuilder().withCost(10).withLoss(0.01).build(r22, r23);
        new Channel.ChannelBuilder().withCost(10).build(r23, r24);
        new Channel.ChannelBuilder().withCost(1).build(r24, server);

        client.updateRoutingTable();

        r11.updateRoutingTable();
        r12.updateRoutingTable();
        r13.updateRoutingTable();
        r14.updateRoutingTable();
        r15.updateRoutingTable();
        r16.updateRoutingTable();
        r17.updateRoutingTable();
        r18.updateRoutingTable();
        r19.updateRoutingTable();
        r110.updateRoutingTable();

        r21.updateRoutingTable();
        r22.updateRoutingTable();
        r23.updateRoutingTable();
        r24.updateRoutingTable();

        server.updateRoutingTable();

        this.numPacketsToSend(server, this.numPacketsToSend);
        EventHandler eventHandler = this.connectAndRun(server, client);


        Assert.assertTrue("client still has packets to send", client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(r11.inputBufferIsEmpty());
        Assert.assertTrue(r12.inputBufferIsEmpty());
        Assert.assertTrue(r13.inputBufferIsEmpty());
        Assert.assertTrue(r14.inputBufferIsEmpty());
        Assert.assertTrue(r15.inputBufferIsEmpty());
        Assert.assertTrue(r16.inputBufferIsEmpty());
        Assert.assertTrue(r17.inputBufferIsEmpty());
        Assert.assertTrue(r18.inputBufferIsEmpty());
        Assert.assertTrue(r19.inputBufferIsEmpty());
        Assert.assertTrue(r110.inputBufferIsEmpty());

        Assert.assertTrue(r21.inputBufferIsEmpty());
        Assert.assertTrue(r22.inputBufferIsEmpty());
        Assert.assertTrue(r23.inputBufferIsEmpty());
        Assert.assertTrue(r24.inputBufferIsEmpty());

        this.allReceived(client, this.numPacketsToSend);

        Assert.assertNull(server.receive());
        this.getStats(server, client);

    }

}
