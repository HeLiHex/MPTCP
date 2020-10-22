package org.example.protocol;

import org.example.data.Flag;
import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class PacketTest {

    @Test
    public void buildPacketBuildsPacketTest(){
        Assert.assertTrue(new Packet.PacketBuilder().build() instanceof Packet);
    }

    @Test
    public void buildPacketWithDestinationBuildsPacketWithDestinationTest(){
        Endpoint endpoint = new BasicTCP(new Random());
        Packet packet = new Packet.PacketBuilder().withDestination(endpoint).build();
        Assert.assertEquals(endpoint, packet.getDestination());
    }

    @Test
    public void buildPacketWithOriginBuildsPacketWithOriginTest(){
        Endpoint endpoint = new BasicTCP(new Random());
        Packet packet = new Packet.PacketBuilder().withOrigin(endpoint).build();
        Assert.assertEquals(endpoint, packet.getOrigin());
    }

    @Test
    public void buildPacketWithMsgBuildsPacketMsgOriginTest(){
        Message msg = new Message( "hello på do!");
        Packet packet = new Packet.PacketBuilder().withPayload(msg).build();
        Assert.assertEquals(msg, packet.getPayload());
    }

    @Test
    public void buildPacketWithSeqNumBuildsPacketSeqNumOriginTest(){
        int seqNum = 123;
        Packet packet = new Packet.PacketBuilder().withSequenceNumber(seqNum).build();
        Assert.assertEquals(seqNum, packet.getSequenceNumber());
    }

    @Test
    public void buildPacketWithAckNumBuildsPacketAckNumOriginTest(){
        int ackNum = 321;
        Packet packet = new Packet.PacketBuilder().withAcknowledgmentNumber(ackNum).build();
        Assert.assertEquals(ackNum, packet.getAcknowledgmentNumber());
    }

    @Test
    public void buildPacketWithFlagBuildsPacketThatHasFlagTest(){
        Flag flag = Flag.ACK;
        Packet packet = new Packet.PacketBuilder().withFlags(flag).build();
        Assert.assertTrue(packet.hasFlag(flag));
    }

    @Test
    public void buildPacketWithFlagsBuildsPacketThatHasFlagsTest(){
        Flag ack = Flag.ACK;
        Flag syn = Flag.SYN;
        Flag fin = Flag.FIN;
        Packet packet = new Packet.PacketBuilder().withFlags(ack, syn, fin).build();
        Assert.assertTrue(packet.hasFlag(syn, fin));
        Assert.assertTrue(packet.hasFlag(ack));
    }

    @Test
    public void buildPacketWithoutFlagBuildsPacketThatDoesNotHaveThatFlagTest(){
        Flag ack = Flag.ACK;
        Flag syn = Flag.SYN;
        Flag fin = Flag.FIN;
        Packet packet = new Packet.PacketBuilder().withFlags(syn, fin).build();
        Assert.assertFalse(packet.hasFlag(syn, fin, ack));
        Assert.assertFalse(packet.hasFlag(ack));
        Assert.assertFalse(packet.hasFlag(syn, ack));
        Assert.assertTrue(packet.hasFlag(syn, fin));
    }
}