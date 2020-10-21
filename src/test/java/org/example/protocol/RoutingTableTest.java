package org.example.protocol;

import org.example.data.Packet;
import org.example.network.Channel;
import org.example.network.Router;
import org.example.network.interfaces.NetworkNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class RoutingTableTest {



    @Test(expected = IllegalStateException.class)
    public void routingTableTrowsIllegalStateExceptionIfNotUpdated(){
        NetworkNode r1 = new Router(100, new Random(), 100);
        NetworkNode r2 = new Router(100, new Random(), 100);
        NetworkNode r3 = new Router(100, new Random(), 100);
        NetworkNode r4 = new Router(100, new Random(), 100);
        r1.addChannel(r2);
        r2.addChannel(r3);
        r3.addChannel(r4);
        r1.route(new Packet.PacketBuilder().withDestination(r4).build());
    }


    @Test(expected = IllegalArgumentException.class)
    public void routingTableTrowsIllegalArgumentExceptionIfDestinationIsNull(){
        NetworkNode r1 = new Router(100, new Random(), 100);
        NetworkNode r2 = new Router(100, new Random(), 100);
        NetworkNode r3 = new Router(100, new Random(), 100);
        NetworkNode r4 = new Router(100, new Random(), 100);

        r1.addChannel(r2);
        r2.addChannel(r3);
        r3.addChannel(r4);

        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();

        r1.route(new Packet.PacketBuilder().withDestination(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void routingTableTrowsIllegalArgumentExceptionIfDestinationIsUnconnected(){
        NetworkNode r1 = new Router(100, new Random(), 100);
        NetworkNode r2 = new Router(100, new Random(), 100);
        NetworkNode r3 = new Router(100, new Random(), 100);
        NetworkNode r4 = new Router(100, new Random(), 100);

        r1.addChannel(r2);
        r2.addChannel(r3);

        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();

        r1.route(new Packet.PacketBuilder().withDestination(r4).build());
    }


    private Channel getChannelFromTo(NetworkNode from, NetworkNode destination){
        for (Channel c : from.getChannels()) {
            if (c.getDestination().equals(destination)) return c;
        }
        Assert.assertFalse(true);
        return null;
    }


    @Test
    public void getPathChoosesShortestPath(){
        for (int i = 0; i < 100; i++) {
            NetworkNode r1 = new Router(100, new Random(), 100);
            NetworkNode r2 = new Router(100, new Random(), 100);
            NetworkNode r3 = new Router(100, new Random(), 100);
            NetworkNode r4 = new Router(100, new Random(), 100);

            r1.addChannel(r2);
            r1.addChannel(r3);
            r2.addChannel(r4);
            r3.addChannel(r4);

            r1.updateRoutingTable();
            r2.updateRoutingTable();
            r3.updateRoutingTable();
            r4.updateRoutingTable();

            Channel channelOnePathOne = getChannelFromTo(r1,r2);
            Channel channelTwoPathOne = getChannelFromTo(r2,r4);

            Channel channelOnePathTwo = getChannelFromTo(r1,r3);
            Channel channelTwoPathTwo = getChannelFromTo(r3,r4);

            int pathOne = channelOnePathOne.getCost() + channelTwoPathOne.getCost();
            int pathTwo = channelOnePathTwo.getCost() + channelTwoPathTwo.getCost();
            if (pathOne == pathTwo) continue;

            Channel usedChannel = pathOne < pathTwo ? channelOnePathOne : channelOnePathTwo;
            Channel notUsedChannel = pathOne > pathTwo ? channelOnePathOne : channelOnePathTwo;

            r1.route(new Packet.PacketBuilder().withOrigin(r1).withDestination(r4).build());

            Assert.assertTrue(!usedChannel.getDestination().inputBufferIsEmpty());
            Assert.assertTrue(notUsedChannel.getDestination().dequeueInputBuffer() == null);
        }
    }

}
