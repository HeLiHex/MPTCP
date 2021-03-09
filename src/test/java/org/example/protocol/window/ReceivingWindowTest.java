package org.example.protocol.window;

import org.example.data.Flag;
import org.example.data.Message;
import org.example.data.Packet;
import org.example.data.PacketBuilder;
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


import java.util.Comparator;
import java.util.Queue;


public class ReceivingWindowTest {

    private ClassicTCP client;
    private ClassicTCP server;

    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    private ReceivingWindow receivingWindow;

    @Before
    public void setup(){
        this.client = new ClassicTCP();
        this.server = new ClassicTCP();
        this.connect(client, server);

        this.receivingWindow = new SelectiveRepeat(10, client.getConnection(), PACKET_COMPARATOR);
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
        Queue<Packet> receivedPackets = this.receivingWindow.getReceivedPackets();
        Assert.assertTrue(receivedPackets.isEmpty());
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

        SendingWindow sendingWindow = new SlidingWindow(10 , client.getConnection(), PACKET_COMPARATOR);
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

        SendingWindow sendingWindow = new SlidingWindow(10 , client.getConnection(), PACKET_COMPARATOR);
        Assert.assertTrue(this.receivingWindow.receive(sendingWindow));

        Assert.assertEquals(packetFromServer, this.receivingWindow.getReceivedPackets().poll());
    }







}
