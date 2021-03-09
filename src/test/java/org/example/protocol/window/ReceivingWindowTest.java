package org.example.protocol.window;

import org.example.data.Packet;
import org.example.protocol.ClassicTCP;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.protocol.window.receiving.SelectiveRepeat;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.util.Comparator;
import java.util.Queue;

public class ReceivingWindowTest {


    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    private ReceivingWindow receivingWindow;

    @Before
    public void setup(){
        ClassicTCP client = new ClassicTCP();
        ClassicTCP server = new ClassicTCP();
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
        Assert.assertTrue(toBeAcked instanceof Packet);
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





}
