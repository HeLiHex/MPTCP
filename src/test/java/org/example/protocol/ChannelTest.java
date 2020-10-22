package org.example.protocol;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.interfaces.NetworkNode;
import org.example.network.Router;
import org.junit.Assert;
import org.junit.Test;

import java.util.PriorityQueue;
import java.util.Random;

public class ChannelTest {

    private Random rand = new Random();
    private NetworkNode source = new Router(100, rand);
    private NetworkNode destination = new Router(100, rand);

    @Test
    public void channelConstructor1Test(){
        Channel channel = new Channel(source, destination, rand, 100);
        Assert.assertTrue(channel instanceof Channel);
        Assert.assertEquals(source, channel.getSource());
        Assert.assertEquals(destination, channel.getDestination());
    }

    @Test
    public void channelConstructor2Test(){
        Channel channel = new Channel(source);
        Assert.assertTrue(channel instanceof Channel);
        Assert.assertEquals(source, channel.getSource());
        Assert.assertEquals(source, channel.getDestination());
        Assert.assertEquals(0, channel.getCost());
    }

    @Test
    public void channelWithConstructor1CostIsPositiveTest(){
        for (int i = 0; i < 10000; i++) {
            Channel channel = new Channel(source, destination, rand, 100);
            Assert.assertTrue(channel.getCost() >= 0);
        }
    }

    @Test
    public void channelWithConstructor2CostIsZeroTest(){
        for (int i = 0; i < 10000; i++) {
            Channel channel = new Channel(source);
            Assert.assertTrue(channel.getCost() == 0);
        }
    }

    @Test
    public void channelPacketDeliversPacketToDestinationNodeTest(){
        Channel channel = new Channel(source, destination, rand, 100);
        Payload payload = new Message( "Test");
        Packet packet = new Packet.PacketBuilder()
                .withOrigin(source)
                .withDestination(destination)
                .withPayload(payload)
                .build();
        channel.channelPackage(packet);
        Packet receivedPacket = destination.dequeueInputBuffer();
        Assert.assertEquals(packet, receivedPacket);
    }


    @Test
    public void lossyChannelDropPacketTest(){
        Channel channel = new Channel(source, destination, rand, 0);
        Payload payload = new Message( "Test");
        Packet packet = new Packet.PacketBuilder()
                .withOrigin(source)
                .withDestination(destination)
                .withPayload(payload)
                .build();
        channel.channelPackage(packet);
        Packet receivedPacket = destination.dequeueInputBuffer();
        Assert.assertEquals(null, receivedPacket);
    }


    @Test
    public void aLittleLossyChannelWillDropPacketAfterAWhileTest(){
        for (int i = 0; i < 10000; i++) {
            Channel channel = new Channel(source, destination, rand, 3);
            Payload payload = new Message( "Test");
            Packet packet = new Packet.PacketBuilder()
                    .withOrigin(source)
                    .withDestination(destination)
                    .withPayload(payload)
                    .build();
            channel.channelPackage(packet);
            Packet receivedPacket = destination.dequeueInputBuffer();
            if (receivedPacket == null){
                Assert.assertTrue(true);
                return;
            }
        }
        Assert.assertFalse(true);
    }

    @Test
    public void channelCompareToWorksInPriorityQueueTest(){
        int size = 1000;
        PriorityQueue<Channel> pq = new PriorityQueue<>(size);
        for (int i = 0; i < size; i++) {
            Channel c = new Channel(source, destination, rand, 0);
            pq.add(c);
        }
        while (pq.size() > 1){
            Assert.assertTrue(pq.poll().getCost() <= pq.peek().getCost());
        }
    }

}
