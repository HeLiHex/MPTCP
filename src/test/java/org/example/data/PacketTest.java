package org.example.data;

import org.example.network.interfaces.Endpoint;
import org.example.protocol.ClassicTCP;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PacketTest {

    @Test
    public void buildPacketBuildsPacketTest(){
        Assert.assertTrue(new PacketBuilder().build() instanceof Packet);
    }

    @Test
    public void buildPacketWithDestinationBuildsPacketWithDestinationTest(){
        Endpoint endpoint = new ClassicTCP(7);
        Packet packet = new PacketBuilder().withDestination(endpoint).build();
        Assert.assertEquals(endpoint, packet.getDestination());
    }

    @Test
    public void buildPacketWithOriginBuildsPacketWithOriginTest(){
        Endpoint endpoint = new ClassicTCP(7);
        Packet packet = new PacketBuilder().withOrigin(endpoint).build();
        Assert.assertEquals(endpoint, packet.getOrigin());
    }

    @Test
    public void buildPacketWithMsgBuildsPacketMsgOriginTest(){
        Message msg = new Message( "hello på do!");
        Packet packet = new PacketBuilder().withPayload(msg).build();
        Assert.assertEquals(msg, packet.getPayload());
    }

    @Test
    public void buildPacketWithSeqNumBuildsPacketSeqNumOriginTest(){
        int seqNum = 123;
        Packet packet = new PacketBuilder().withSequenceNumber(seqNum).build();
        Assert.assertEquals(seqNum, packet.getSequenceNumber());
    }

    @Test
    public void buildPacketWithAckNumBuildsPacketAckNumOriginTest(){
        int ackNum = 321;
        Packet packet = new PacketBuilder().withAcknowledgmentNumber(ackNum).withFlags(Flag.ACK).build();
        Assert.assertEquals(ackNum, packet.getAcknowledgmentNumber());
    }

    @Test
    public void buildPacketWithFlagBuildsPacketThatHasFlagTest(){
        Flag flag = Flag.ACK;
        Packet packet = new PacketBuilder().withFlags(flag).build();
        Assert.assertTrue(packet.hasAllFlags(flag));
    }

    @Test
    public void buildPacketWithFlagsBuildsPacketThatHasFlagsTest(){
        Flag ack = Flag.ACK;
        Flag syn = Flag.SYN;
        Flag fin = Flag.FIN;
        Packet packet = new PacketBuilder().withFlags(ack, syn, fin).build();
        Assert.assertTrue(packet.hasAllFlags(syn, fin));
        Assert.assertTrue(packet.hasAllFlags(ack));
    }

    @Test
    public void buildPacketWithoutFlagBuildsPacketThatDoesNotHaveThatFlagTest(){
        Flag ack = Flag.ACK;
        Flag syn = Flag.SYN;
        Flag fin = Flag.FIN;
        Packet packet = new PacketBuilder().withFlags(syn, fin).build();
        Assert.assertFalse(packet.hasAllFlags(syn, fin, ack));
        Assert.assertFalse(packet.hasAllFlags(ack));
        Assert.assertFalse(packet.hasAllFlags(syn, ack));
        Assert.assertTrue(packet.hasAllFlags(syn, fin));
    }

    @Test
    public void buildPacketWithDuplicateFlagsBuildsPacketThatHasOneOfThatFlagTest(){
        Flag ack = Flag.ACK;
        Flag syn = Flag.SYN;
        Flag fin = Flag.FIN;
        Packet packet = new PacketBuilder().withFlags(ack, syn, syn, fin, fin).build();
        List<Flag> flags = packet.getFlags();
        Assert.assertEquals(3, flags.size());
        Assert.assertTrue(packet.hasAllFlags(syn, fin, ack));
    }

    @Test
    public void buildPacketWithPayloadHasCorrectSizeTest(){
        Message message = new Message("test");
        Packet packet = new PacketBuilder()
                .withPayload(message)
                .build();
        Assert.assertEquals(message.size(), packet.size());
    }

    @Test
    public void buildPacketWithoutPayloadHasZeroSizeTest(){
        Packet packet = new PacketBuilder()
                .build();
        Assert.assertEquals(0, packet.size());
    }

}