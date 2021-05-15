import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.network.address.SimpleAddress;
import org.example.protocol.ClassicTCP;
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
    public void setUp(){
        Util.resetTime();
        Util.setSeed(1337);
    }

    private void numPacketsToSend(TCP tcpToSend, int numPacketsToSend){
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            tcpToSend.send(msg);
        }
    }

    private EventHandler connectAndRun(TCP client, TCP server){
        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
        return eventHandler;
    }

    private void allReceived(TCP receiver, int numPacketsToSend){
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            Packet received = receiver.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }
    }

    @Test
    public void shortPathLowLoss(){
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-ShortPathLowLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 4")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-ShortPathLowLoss")).build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().withLoss(0.001).build(r2, r3);
        new Channel.ChannelBuilder().build(r3, r4);
        new Channel.ChannelBuilder().build(r4, server);

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

        eventHandler.printStatistics();
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
    public void shortPathHighLoss(){
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-ShortPathHighLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 4")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-ShortPathHighLoss")).build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().withLoss(0.01).build(r2, r3);
        new Channel.ChannelBuilder().build(r3, r4);
        new Channel.ChannelBuilder().build(r4, server);

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

        eventHandler.printStatistics();
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
    public void longPathLowLoss(){
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-LongPathLowLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 4")).build();
        Routable r5 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 5")).build();
        Routable r6 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 6")).build();
        Routable r7 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 7")).build();
        Routable r8 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 8")).build();
        Routable r9 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 9")).build();
        Routable r10 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 10")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-LongPathLowLoss")).build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().build(r2, r3);
        new Channel.ChannelBuilder().build(r3, r4);
        new Channel.ChannelBuilder().build(r4, r5);
        new Channel.ChannelBuilder().withLoss(0.001).build(r5, r6);
        new Channel.ChannelBuilder().build(r6, r7);
        new Channel.ChannelBuilder().build(r7, r8);
        new Channel.ChannelBuilder().build(r8, r9);
        new Channel.ChannelBuilder().build(r9, r10);
        new Channel.ChannelBuilder().build(r10, server);

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

        eventHandler.printStatistics();
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
    public void longPathHighLoss(){
        ClassicTCP client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Client-LongPathHighLoss")).build();
        Routable r1 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 1")).build();
        Routable r2 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 2")).build();
        Routable r3 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 3")).build();
        Routable r4 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 4")).build();
        Routable r5 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 5")).build();
        Routable r6 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 6")).build();
        Routable r7 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 7")).build();
        Routable r8 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 8")).build();
        Routable r9 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 9")).build();
        Routable r10 = new Router.RouterBuilder().withAverageQueueUtilization(0.98).withAddress(new SimpleAddress("Router 10")).build();
        ClassicTCP server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(30).setReno().withAddress(new SimpleAddress("Server-LongPathHighLoss")).build();

        new Channel.ChannelBuilder().build(client, r1);
        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().build(r2, r3);
        new Channel.ChannelBuilder().build(r3, r4);
        new Channel.ChannelBuilder().build(r4, r5);
        new Channel.ChannelBuilder().withLoss(0.01).build(r5, r6);
        new Channel.ChannelBuilder().build(r6, r7);
        new Channel.ChannelBuilder().build(r7, r8);
        new Channel.ChannelBuilder().build(r8, r9);
        new Channel.ChannelBuilder().build(r9, r10);
        new Channel.ChannelBuilder().build(r10, server);

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

        eventHandler.printStatistics();
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

}
