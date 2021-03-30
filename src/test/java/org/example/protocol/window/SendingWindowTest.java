package org.example.protocol.window;

import org.example.data.Message;
import org.example.data.Packet;
import org.example.protocol.ClassicTCP;
import org.example.protocol.window.sending.SendingWindow;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.TCPConnectEvent;
import org.example.simulator.events.tcp.TCPInputEvent;
import org.junit.*;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;


public class SendingWindowTest {

    private ClassicTCP client;
    private ClassicTCP server;
    private SendingWindow sendingWindow;
    private EventHandler eventHandler;

    @Rule
    public Timeout globalTimeout = new Timeout(30, TimeUnit.SECONDS);

    @Before
    public void setup() throws IllegalAccessException {
        this.client = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(20).setTahoe().build();
        this.server = new ClassicTCP.ClassicTCPBuilder().withReceivingWindowCapacity(20).setTahoe().build();
        this.connect(client, server);

        this.sendingWindow = this.client.getSendingWindow();

    }

    private void connect(ClassicTCP client, ClassicTCP server){
        client.addChannel(server);
        client.updateRoutingTable();
        server.updateRoutingTable();

        this.eventHandler = new EventHandler();
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
        for (int i = 0; i < this.client.getThisReceivingWindowCapacity(); i++) {
            this.client.send(new Message("test " + i));
        }
        for (int i = 0; i < this.client.getThisReceivingWindowCapacity(); i++) {
            this.client.trySend();
        }
        Assert.assertTrue(this.sendingWindow.isWaitingForAck());
    }

    @Test
    public void isWaitingForAckIsFalseIfSendingWindowIsAlmostFullTest(){
        for (int i = 0; i < Math.pow(this.server.getThisReceivingWindowCapacity(), 2); i++) {
            this.client.send(new Message("test " + i));
            this.sendingWindow.increase();
        }
        for (int i = 0; i < this.client.getOtherReceivingWindowCapacity() - 1; i++) {
            this.client.trySend();
        }
        Assert.assertEquals(this.client.getOtherReceivingWindowCapacity(), this.sendingWindow.getWindowCapacity());
        Assert.assertFalse(this.sendingWindow.isWaitingForAck());
    }

    @Test
    public void windowCantIncreaseToMoreThanServersReceivingWindow(){
        for (int i = 0; i < Math.pow(this.server.getThisReceivingWindowCapacity(), 2) * 10; i++) {
            this.sendingWindow.increase();
        }
        Assert.assertEquals(this.server.getThisReceivingWindowCapacity(), this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void windowCanDecreaseWhenInMaxCapacity(){
        for (int i = 0; i < Math.pow(this.server.getThisReceivingWindowCapacity(), 2); i++) {
            this.sendingWindow.increase();
        }
        this.sendingWindow.decrease();
        Assert.assertEquals(1, this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void initWindowCanNotDecrease(){
        this.sendingWindow.decrease();
        Assert.assertEquals(1, this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void windowWillDecreaseToDefaultValueIfDecreasedEnough(){
        for (int i = 0; i < this.server.getThisReceivingWindowCapacity(); i++) {
            this.sendingWindow.increase();
        }

        for (int i = 0; i < this.server.getThisReceivingWindowCapacity(); i++) {
            this.sendingWindow.decrease();
        }
        Assert.assertEquals(1, this.sendingWindow.getWindowCapacity());
    }

    @Test
    public void floodWithPacketsInLossyChannelShouldResultInVariableWindowCapacity() throws IllegalAccessException {
        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.isConnected());

        System.out.println("connected");

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        //Assert.assertTrue(router.inputBufferIsEmpty());

        int numPacketsToSend = server.getThisReceivingWindowCapacity() * 100;
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
                Assert.assertEquals(1, curWindowCapacity);
            }else if (packetAcked){
                Assert.assertTrue(this.client.getOtherReceivingWindowCapacity() >= curWindowCapacity);
            }else{
                Assert.assertEquals(prevWindowCapacity, curWindowCapacity);
            }
            prevWindowCapacity = curWindowCapacity;
        }

        Assert.assertTrue(client.inputBufferIsEmpty());
        Assert.assertTrue(server.inputBufferIsEmpty());
        Assert.assertTrue(client.outputBufferIsEmpty());
        Assert.assertTrue(server.outputBufferIsEmpty());
        //Assert.assertTrue(router.inputBufferIsEmpty());

        for (int i = 1; i <= numPacketsToSend; i++) {
            Message msg = new Message( "test " + i);
            Packet received = server.receive();
            Assert.assertNotNull(received);
            Assert.assertEquals(msg, received.getPayload());
        }

    }



}
