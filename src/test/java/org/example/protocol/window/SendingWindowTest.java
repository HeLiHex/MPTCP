package org.example.protocol.window;

import org.example.data.Message;
import org.example.protocol.ClassicTCP;
import org.example.protocol.window.sending.SendingWindow;
import org.example.simulator.EventHandler;
import org.example.simulator.events.tcp.TCPConnectEvent;
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
        for (int i = 0; i < this.server.getWindowSize() * 10; i++) {
            this.sendingWindow.increase();
        }
        this.sendingWindow.decrease();
        Assert.assertEquals((int)Math.ceil(this.server.getWindowSize()/2), this.sendingWindow.getWindowCapacity());
    }


}
