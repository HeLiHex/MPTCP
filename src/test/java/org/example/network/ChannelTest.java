package org.example.network;

import org.example.data.*;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.ClassicTCP;
import org.junit.Assert;
import org.junit.Test;

import java.util.PriorityQueue;

public class ChannelTest {

    private Endpoint source = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
    private Endpoint destination = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

    @Test
    public void channelConstructor1Test() {
        Channel channel = new Channel(source, destination, 100);
        Assert.assertTrue(channel instanceof Channel);
        Assert.assertEquals(source, channel.getSource());
        Assert.assertEquals(destination, channel.getDestination());
    }

    @Test
    public void channelConstructor2Test() {
        Channel channel = new Channel(source);
        Assert.assertTrue(channel instanceof Channel);
        Assert.assertEquals(source, channel.getSource());
        Assert.assertEquals(source, channel.getDestination());
        Assert.assertEquals(0, channel.getCost());
    }

    @Test
    public void channelWithConstructor1CostIsPositiveTest() {
        for (int i = 0; i < 10000; i++) {
            Channel channel = new Channel(source, destination, 100);
            Assert.assertTrue(channel.getCost() >= 0);
        }
    }

    @Test
    public void channelWithConstructor2CostIsZeroTest() {
        for (int i = 0; i < 10000; i++) {
            Channel channel = new Channel(source);
            Assert.assertEquals(0, channel.getCost());
        }
    }

    @Test
    public void channelPacketDeliversPacketToDestinationNodeTest() {
        Channel channel = new Channel(source, destination, 0);
        Payload payload = new Message("Test");
        Packet packet = new PacketBuilder()
                .withOrigin(source)
                .withDestination(destination)
                .withPayload(payload)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build();
        channel.channelPackage(packet);
        Assert.assertTrue(channel.channel());
        Packet receivedPacket = destination.dequeueInputBuffer();
        Assert.assertEquals(packet, receivedPacket);
    }


    @Test
    public void lossyChannelDropPacketTest() {
        Channel channel = new Channel(source, destination, 0);
        Payload payload = new Message("Test");
        Packet packet = new PacketBuilder()
                .withOrigin(source)
                .withDestination(destination)
                .withPayload(payload)
                .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                .build();
        channel.channelPackage(packet);
        Packet receivedPacket = destination.dequeueInputBuffer();
        Assert.assertEquals(null, receivedPacket);
    }


    @Test
    public void aLittleLossyChannelWillDropPacketAfterAWhileTest() {
        for (int i = 0; i < 10000; i++) {
            Channel channel = new Channel(source, destination, 3);
            Payload payload = new Message("Test");
            Packet packet = new PacketBuilder()
                    .withOrigin(source)
                    .withDestination(destination)
                    .withPayload(payload)
                    .withFlags(Flag.SYN) // hack to overcome connection check in the endpoints
                    .build();
            channel.channelPackage(packet);
            Packet receivedPacket = destination.dequeueInputBuffer();
            if (receivedPacket == null) {
                Assert.assertTrue(true);
                return;
            }
        }
        Assert.assertFalse(true);
    }

    @Test
    public void channelCompareToWorksInPriorityQueueTest() {
        int size = 1000;
        PriorityQueue<Channel> pq = new PriorityQueue<>(size);
        for (int i = 0; i < size; i++) {
            Channel c = new Channel(source, destination, 0);
            pq.add(c);
        }
        while (pq.size() > 1) {
            Assert.assertTrue(pq.poll().getCost() <= pq.peek().getCost());
        }
    }

    @Test
    public void differentChannelsAreNotEqualTest() {
        Endpoint source1 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Endpoint destination1 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        Endpoint source2 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        Endpoint destination2 = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();

        Channel channel1 = new Channel(source1, destination1, 100);
        Channel channel2 = new Channel(source2, destination2, 100);

        Assert.assertNotEquals(channel1, channel2);
    }


}
