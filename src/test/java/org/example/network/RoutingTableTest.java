package org.example.network;

import org.example.data.PacketBuilder;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.ClassicTCP;
import org.junit.Assert;
import org.junit.Test;

public class RoutingTableTest {

    @Test(expected = IllegalStateException.class)
    public void routingTableTrowsIllegalStateExceptionIfNotUpdated() {
        Endpoint r1 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        NetworkNode r2 = new Router.RouterBuilder().build();
        NetworkNode r3 = new Router.RouterBuilder().build();
        Endpoint r4 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().build(r2, r3);
        new Channel.ChannelBuilder().build(r3, r4);

        r1.route(new PacketBuilder().withDestination(r4).build());
    }


    @Test(expected = IllegalArgumentException.class)
    public void routingTableTrowsIllegalArgumentExceptionIfDestinationIsNull() {
        NetworkNode r1 = new Router.RouterBuilder().build();
        NetworkNode r2 = new Router.RouterBuilder().build();
        NetworkNode r3 = new Router.RouterBuilder().build();
        NetworkNode r4 = new Router.RouterBuilder().build();

        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().build(r2, r3);
        new Channel.ChannelBuilder().build(r3, r4);

        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();

        r1.route(new PacketBuilder().withDestination(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void routingTableTrowsIllegalArgumentExceptionIfDestinationIsUnconnected() {
        Endpoint r1 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        NetworkNode r2 = new Router.RouterBuilder().build();
        NetworkNode r3 = new Router.RouterBuilder().build();
        Endpoint r4 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        new Channel.ChannelBuilder().build(r1, r2);
        new Channel.ChannelBuilder().build(r2, r3);

        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();

        r1.route(new PacketBuilder().withDestination(r4).build());
    }


    private Channel getChannelFromTo(NetworkNode from, NetworkNode destination) {
        for (Channel c : from.getChannels()) {
            if (c.getDestination().equals(destination)) return c;
        }
        Assert.assertFalse(true);
        return null;
    }


    @Test
    public void getPathChoosesShortestPathTest() {
        for (int i = 0; i < 100; i++) {
            Endpoint r1 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
            NetworkNode r2 = new Router.RouterBuilder().build();
            NetworkNode r3 = new Router.RouterBuilder().build();
            Endpoint r4 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

            new Channel.ChannelBuilder().build(r1, r2);
            new Channel.ChannelBuilder().build(r1, r3);
            new Channel.ChannelBuilder().build(r2, r4);
            new Channel.ChannelBuilder().build(r3, r4);

            r1.updateRoutingTable();
            r2.updateRoutingTable();
            r3.updateRoutingTable();
            r4.updateRoutingTable();

            Channel channelOnePathOne = getChannelFromTo(r1, r2);
            Channel channelTwoPathOne = getChannelFromTo(r2, r4);

            Channel channelOnePathTwo = getChannelFromTo(r1, r3);
            Channel channelTwoPathTwo = getChannelFromTo(r3, r4);

            int pathOne = channelOnePathOne.getCost() + channelTwoPathOne.getCost();
            int pathTwo = channelOnePathTwo.getCost() + channelTwoPathTwo.getCost();
            if (pathOne == pathTwo) continue;

            Channel usedChannel = pathOne < pathTwo ? channelOnePathOne : channelOnePathTwo;
            Channel notUsedChannel = pathOne > pathTwo ? channelOnePathOne : channelOnePathTwo;

            r1.route(new PacketBuilder().withOrigin(r1).withDestination(r4).build());

            Assert.assertTrue(usedChannel.channel());
            Assert.assertFalse(notUsedChannel.channel());

            Assert.assertTrue(!usedChannel.getDestination().inputBufferIsEmpty());
            Assert.assertNull(notUsedChannel.getDestination().dequeueInputBuffer());
        }
    }


    @Test
    public void toStringTest() {
        RoutingTable routingTable = new RoutingTable();
        routingTable.update(new Router.RouterBuilder().build());
        routingTable.toString();
        Assert.assertTrue(routingTable.toString() instanceof String);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathWhenNoPathIsThereTest() {
        RoutingTable routingTable = new RoutingTable();
        routingTable.update(new Router.RouterBuilder().build());
        routingTable.getPath(new Router.RouterBuilder().build(), new Router.RouterBuilder().build());
    }

}
