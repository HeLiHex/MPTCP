package org.example.protocol.window;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.network.Routable;
import org.example.network.Router;
import org.example.protocol.ClassicTCP;
import org.example.protocol.window.sending.SendingWindow;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SendingWindowTest {

    private ClassicTCP client;
    private ClassicTCP server;
    private SendingWindow sendingWindow;

    @Before
    public void setup() throws IllegalAccessException {
        this.client = new ClassicTCP();
        this.server = new ClassicTCP();
        this.connect(client, server);

        this.sendingWindow = this.client.getSendingWindow();

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
    public void initCorrectWindowSizeTest(){
        Assert.assertEquals(1, this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void initIsWaitingForAckIsFalseIfNoPacketsAreSentTest(){
        Assert.assertFalse(this.sendingWindow.isWaitingForAck());
    }

    @Test
    public void initIsWaitingForAckIsTrueIfSendingWindowIsFullTest(){
        for (int i = 0; i < this.client.getWindowSize(); i++) {
            this.client.send(new Message("test " + i));
        }
        for (int i = 0; i < this.client.getWindowSize(); i++) {
            this.client.trySend();
        }
        Assert.assertTrue(this.sendingWindow.isWaitingForAck());
    }

    @Test
    public void isWaitingForAckIsFalseIfSendingWindowIsAlmostFullTest(){
        for (int i = 0; i < this.client.getWindowSize(); i++) {
            this.client.send(new Message("test " + i));
            this.sendingWindow.increase();
        }
        for (int i = 0; i < this.client.getWindowSize() - 1; i++) {
            this.client.trySend();
        }
        Assert.assertEquals(this.client.getWindowSize(), this.sendingWindow.getWindowCapacity());
        Assert.assertFalse(this.sendingWindow.isWaitingForAck());
    }

    @Test
    public void windowCantIncreaseToMoreThanServersReceivingWindow(){
        for (int i = 0; i < this.server.getWindowSize() * 10; i++) {
            this.sendingWindow.increase();
        }
        Assert.assertEquals(this.server.getWindowSize(), this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void windowCanDecreaseWhenInMaxCapacity(){
        for (int i = 0; i < this.server.getWindowSize(); i++) {
            this.sendingWindow.increase();
        }
        this.sendingWindow.decrease();
        Assert.assertEquals((int)Math.ceil(this.server.getWindowSize()/2.0), this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void initWindowCanNotDecrease(){
        this.sendingWindow.decrease();
        Assert.assertEquals(1, this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void windowWillDecreaseToDefaultValueIfDecreasedEnough(){
        for (int i = 0; i < this.server.getWindowSize(); i++) {
            this.sendingWindow.increase();
        }

        for (int i = 0; i < this.server.getWindowSize(); i++) {
            this.sendingWindow.decrease();
        }
        Assert.assertEquals(1, this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void floodWithPacketsInLossyChannelShouldResultInVariableWindowCapacity() throws IllegalAccessException {
        ClassicTCP client = new ClassicTCP();
        Routable router = new Router.RouterBuilder().withNoiseTolerance(2).build();
        ClassicTCP server = new ClassicTCP();

        client.addChannel(router);
        router.addChannel(server);

        client.updateRoutingTable();
        router.updateRoutingTable();
        server.updateRoutingTable();

        EventHandler eventHandler = new EventHandler();
        eventHandler.addEvent(new TCPConnectEvent(client, server));
        eventHandler.run();

        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.isConnected());

        System.out.println("connected");

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(router.inputBufferIsEmpty());

        int numPacketsToSend = server.getWindowSize() * 1000;
        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message("test " + i);
            client.send(msg);
        }

        int prevWindowCapacity = client.getSendingWindow().getWindowCapacity();
        eventHandler.addEvent(new TCPInputEvent(client));
        while (eventHandler.singleRun()){
            int curWindowCapacity = client.getSendingWindow().getWindowCapacity();

            boolean loss = curWindowCapacity < prevWindowCapacity;
            boolean packetAcked = curWindowCapacity > prevWindowCapacity;

            if (loss){
                Assert.assertEquals((int) Math.ceil(prevWindowCapacity/2.0), curWindowCapacity);
            }else if (packetAcked){
                Assert.assertTrue(this.client.getWindowSize() >= curWindowCapacity);
            }else{
                Assert.assertEquals(prevWindowCapacity, curWindowCapacity);
            }
            prevWindowCapacity = curWindowCapacity;
        }

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        Assert.assertTrue(router.inputBufferIsEmpty());

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }

    }



}
