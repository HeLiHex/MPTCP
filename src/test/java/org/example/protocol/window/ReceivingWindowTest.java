package org.example.protocol.window;

import org.example.data.*;
import org.example.protocol.ClassicTCP;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.protocol.window.receiving.SelectiveRepeat;
import org.example.protocol.window.sending.SendingWindow;
import org.example.protocol.window.sending.SlidingWindow;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.util.*;


public class ReceivingWindowTest {

    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);

    private ClassicTCP client;
    private ClassicTCP server;
    private ReceivingWindow receivingWindow;
    private Queue<Packet> receivedPackets;
    private List<Payload> payloadsToSend;

    @Before
    public void setup(){
        this.receivedPackets = new PriorityQueue<>(PACKET_COMPARATOR);
        this.payloadsToSend = new ArrayList<>();
        this.client = new ClassicTCP(7);
        this.server = new ClassicTCP(7);
        this.connect(client, server);

        this.receivingWindow = new SelectiveRepeat(10, client.getConnection(), PACKET_COMPARATOR, this.receivedPackets);
    }

    private void connect(ClassicTCP client, ClassicTCP server){
        client.addChannel(server);
        client.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();
    }



    @Test
    public void ackThisReturnsPacketTest(){
        Packet toBeAcked = this.receivingWindow.ackThis();
        Assert.assertNotNull(toBeAcked);
    }


    @Test
    public void shouldAckReturnsTrueTest(){
        Assert.assertTrue(this.receivingWindow.shouldAck());
    }

    @Test
    public void getReceivedPacketsShouldReturnEmptyQueueWhenNoPacketsAreReceived(){
        Assert.assertTrue(this.receivedPackets.isEmpty());
    }

    @Test
    public void receiveShouldReturnFalseIfNoPacketsAreReceived(){
        Assert.assertFalse(this.receivingWindow.receive(null));
    }



    @Test(expected = NullPointerException.class)
    public void receiveAckWithNullSendingWindowArgumentCausesNullPointerException(){
        Packet ackFromServer = new PacketBuilder()
                .withSequenceNumber(this.client.getConnection().getNextAcknowledgementNumber()) //hack to find clients expected next packet sequence number
                .withFlags(Flag.ACK)
                .withConnection(this.server.getConnection())
                .build();
        Assert.assertTrue(this.receivingWindow.inReceivingWindow(ackFromServer));
        this.receivingWindow.offer(ackFromServer);
        Assert.assertFalse(this.receivingWindow.receive(null));
    }

    @Test
    public void receiveReturnsFalseIfAckIsReceived(){
        Packet ackFromServer = new PacketBuilder()
                .withSequenceNumber(this.client.getConnection().getNextAcknowledgementNumber()) //hack to find clients expected next packet sequence number
                .withFlags(Flag.ACK)
                .withConnection(this.server.getConnection())
                .build();

        Assert.assertTrue(this.receivingWindow.inReceivingWindow(ackFromServer));
        this.receivingWindow.offer(ackFromServer);

        SendingWindow sendingWindow = new SlidingWindow(10 , true, client.getConnection(), PACKET_COMPARATOR, this.payloadsToSend);
        Assert.assertFalse(this.receivingWindow.receive(sendingWindow));
    }

    @Test
    public void receiveReturnsTrueIfPacketIsReceived(){
        Packet packetFromServer = new PacketBuilder()
                .withSequenceNumber(this.client.getConnection().getNextAcknowledgementNumber()) //hack to find clients expected next packet sequence number
                .withPayload(new Message("test"))
                .withConnection(this.server.getConnection())
                .build();

        Assert.assertTrue(this.receivingWindow.inReceivingWindow(packetFromServer));
        this.receivingWindow.offer(packetFromServer);

        SendingWindow sendingWindow = new SlidingWindow(10 , true, client.getConnection(), PACKET_COMPARATOR, this.payloadsToSend);
        Assert.assertTrue(this.receivingWindow.receive(sendingWindow));

        Assert.assertEquals(packetFromServer, this.receivedPackets.poll());
    }







}
