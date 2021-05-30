package org.example.protocol.window;

import org.example.data.*;
import org.example.network.Channel;
import org.example.protocol.ClassicTCP;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.protocol.window.receiving.SelectiveRepeat;
import org.example.protocol.window.sending.SendingWindow;
import org.example.protocol.window.sending.SlidingWindow;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.statistics.TCPStats;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ReceivingWindowTest {

    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    @Rule
    public Timeout globalTimeout = new Timeout(30, TimeUnit.SECONDS);
    private ClassicTCP client;
    private ClassicTCP server;
    private ReceivingWindow receivingWindow;
    private List<Packet> receivedPackets;
    private List<Pair<Integer, Payload>> payloadsToSend;

    @Before
    public void setup() {
        this.receivedPackets = new ArrayList<>();
        this.payloadsToSend = new ArrayList<>();
        this.client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        this.server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(7).build();
        this.connect(client, server);

        this.receivingWindow = new SelectiveRepeat(10, PACKET_COMPARATOR, this.receivedPackets);
    }

    private void connect(ClassicTCP client, ClassicTCP server) {
        new Channel.ChannelBuilder().build(client, server);
        client.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
    }


    @Test
    public void ackThisReturnsPacketTest() {
        Packet toBeAcked = this.receivingWindow.ackThis(this.client);
        Assert.assertNotNull(toBeAcked);
    }


    @Test
    public void shouldAckReturnsTrueTest() {
        Assert.assertTrue(this.receivingWindow.shouldAck());
    }

    @Test
    public void getReceivedPacketsShouldReturnEmptyQueueWhenNoPacketsAreReceived() {
        Assert.assertTrue(this.receivedPackets.isEmpty());
    }

    @Test
    public void receiveShouldReturnFalseIfNoPacketsAreReceived() {
        Assert.assertFalse(this.receivingWindow.receive(null));
    }


    @Test(expected = NullPointerException.class)
    public void receiveAckWithNullSendingWindowArgumentCausesNullPointerException() {
        Packet ackFromServer = new PacketBuilder()
                .withSequenceNumber(this.client.getConnection().getNextAcknowledgementNumber()) //hack to find clients expected next packet sequence number
                .withFlags(Flag.ACK)
                .withConnection(this.server.getConnection())
                .build();
        Assert.assertTrue(this.receivingWindow.inReceivingWindow(ackFromServer, client.getConnection()));
        this.receivingWindow.offer(ackFromServer);
        Assert.assertFalse(this.receivingWindow.receive(null));
    }

    @Test
    public void receiveReturnsFalseIfAckIsReceived() {
        Packet ackFromServer = new PacketBuilder()
                .withSequenceNumber(this.client.getConnection().getNextAcknowledgementNumber())
                .withFlags(Flag.ACK)
                .withConnection(this.server.getConnection())
                .build();

        Assert.assertTrue(this.receivingWindow.inReceivingWindow(ackFromServer, client.getConnection()));
        this.receivingWindow.offer(ackFromServer);

        SendingWindow sendingWindow = new SlidingWindow(10, true, client.getConnection(), PACKET_COMPARATOR, this.payloadsToSend, new TCPStats(this.client.getAddress()));
        Assert.assertFalse(this.receivingWindow.receive(sendingWindow));
    }

    @Test
    public void receiveReturnsTrueIfPacketIsReceived() {
        Packet packetFromServer = new PacketBuilder()
                .withSequenceNumber(this.client.getConnection().getNextAcknowledgementNumber())
                .withPayload(new Message("test"))
                .withConnection(this.server.getConnection())
                .build();

        Assert.assertTrue(this.receivingWindow.inReceivingWindow(packetFromServer, client.getConnection()));
        this.receivingWindow.offer(packetFromServer);

        SendingWindow sendingWindow = new SlidingWindow(10, true, client.getConnection(), PACKET_COMPARATOR, this.payloadsToSend, new TCPStats(this.client.getAddress()));
        Assert.assertTrue(this.receivingWindow.receive(sendingWindow));

        Assert.assertEquals(packetFromServer, this.receivedPackets.remove(0));
    }


}
