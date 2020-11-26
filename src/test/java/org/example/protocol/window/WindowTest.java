package org.example.protocol.window;

import org.example.data.Flag;
import org.example.data.Packet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WindowTest {


    private void fillWindow(Window window){
        for (int i = 0; i < window.windowSize(); i++) {
            window.add(new Packet.PacketBuilder().build());
        }
    }


    @Test
    public void ackReceivedWithAckThatMatchAckPacketTest(){
        Window window = new BasicWindow(5, 5);
        for (int i = 0; i < window.windowSize(); i++) {
            window.add(new Packet.PacketBuilder()
                    .withSequenceNumber(i)
                    .build());
        }
        Packet toSend = window.getPacketToSend();

        Packet ack = new Packet.PacketBuilder()
                .withAcknowledgmentNumber(toSend.getSequenceNumber())
                .withFlags(Flag.ACK)
                .build();
        window.ackReceived(ack);

        Assert.assertEquals(window.windowSize() - 1, ((Queue)window).size());
    }

    @Test
    public void ackReceivedWithAckThatMatchButPacketNotActiveAckNothingTest(){
        Window window = new BasicWindow(5, 5);
        for (int i = 0; i < window.windowSize(); i++) {
            window.add(new Packet.PacketBuilder()
                    .withSequenceNumber(i)
                    .build());
        }

        Packet ack = new Packet.PacketBuilder()
                .withAcknowledgmentNumber(1)
                .withFlags(Flag.ACK)
                .build();
        window.ackReceived(ack);

        Assert.assertEquals(window.windowSize(), ((Queue)window).size());
    }

    @Test
    public void ackReceivedWithAckThatDontMatchAckNothingTest(){
        Window window = new BasicWindow(5, 5);
        for (int i = 0; i < window.windowSize(); i++) {
            window.add(new Packet.PacketBuilder()
                    .withSequenceNumber(i)
                    .build());
        }
        Packet ack = new Packet.PacketBuilder()
                .withAcknowledgmentNumber(1000000)
                .withSequenceNumber(1000000)
                .withFlags(Flag.ACK)
                .build();
        window.ackReceived(ack);

        Assert.assertEquals(window.windowSize(), ((Queue)window).size());
    }

    @Test
    public void getPacketToSendWithSomePacketsInWindowReturnsPacketTest(){
        Window window = new BasicWindow(5, 5);
        this.fillWindow(window);
        ((Queue)window).remove();
        ((Queue)window).remove();
        Assert.assertTrue(window.getPacketToSend() instanceof Packet);
    }

    @Test
    public void getPacketToSendWithFullWindowReturnsPacketTest(){
        Window window = new BasicWindow(5, 5);
        this.fillWindow(window);
        Assert.assertTrue(window.getPacketToSend() instanceof Packet);
    }

    @Test
    public void getPacketToSendWithEmptyWindowReturnsNullTest(){
        Window window = new BasicWindow(5, 5);
        Assert.assertEquals(null, window.getPacketToSend());
    }

    @Test
    public void updateTimersUpdatesTimersCorrectlyIfTimerIsStartedTest(){
        int timerDuration = 10;
        Window window = new BasicWindow(5, timerDuration);
        fillWindow(window);
        for (WindowEntry entry : window){
            entry.getPacketTimeout().start();
        }
        window.updateTimers();
        for (WindowEntry entry : window){
            Assert.assertEquals(timerDuration - 1, entry.getPacketTimeout().getTimeoutValue());
        }
    }

    @Test
    public void updateTimersOnWaitingTimersDoesNotUpdateTest(){
        int timerDuration = 10;
        Window window = new BasicWindow(5, timerDuration);
        this.fillWindow(window);
        window.updateTimers();
        for (WindowEntry entry : window){
            Assert.assertEquals(timerDuration, entry.getPacketTimeout().getTimeoutValue());
        }
    }


    @Test
    public void addPacketToWindowWorksTest(){
        int windowSize = 5;
        Window window = new BasicWindow(windowSize, 1);
        window.add(new Packet.PacketBuilder().build());
        ArrayBlockingQueue<WindowEntry> queue = (ArrayBlockingQueue) window;
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void addMultiplePacketsToWindowWorksTest(){
        int windowSize = 10;
        Window window = new BasicWindow(windowSize, 1);

        int numberOfPackets = 5;
        for (int i = 0; i < numberOfPackets; i++) {
            window.add(new Packet.PacketBuilder().build());
        }
        ArrayBlockingQueue<WindowEntry> queue = (ArrayBlockingQueue) window;
        Assert.assertEquals(numberOfPackets, queue.size());
    }

    @Test
    public void add5PacketsToWindowWithWindowSize5WorksTest(){
        int windowSize = 5;
        Window window = new BasicWindow(windowSize, 1);

        int numberOfPackets = 5;
        for (int i = 0; i < numberOfPackets; i++) {
            window.add(new Packet.PacketBuilder().build());
        }
        ArrayBlockingQueue<WindowEntry> queue = (ArrayBlockingQueue) window;
        Assert.assertEquals(numberOfPackets, queue.size());
    }

    @Test(expected = IllegalStateException.class)
    public void add5PacketsToWindowWithWindowSize4DoesNotWorkTest(){
        int windowSize = 4;
        Window window = new BasicWindow(windowSize, 1);

        int numberOfPackets = 5;
        for (int i = 0; i < numberOfPackets; i++) {
            window.add(new Packet.PacketBuilder().build());
        }
        ArrayBlockingQueue<WindowEntry> queue = (ArrayBlockingQueue) window;
    }

    @Test
    public void addPacketToWindowAddsCorrectPacketTest(){
        int windowSize = 5;
        Window window = new BasicWindow(windowSize, 1);
        Packet packet = new Packet.PacketBuilder().build();
        window.add(packet);
        ArrayBlockingQueue<WindowEntry> queue = (ArrayBlockingQueue) window;
        Assert.assertEquals(packet, queue.poll().getPacket());
    }

    @Test
    public void addPacketToWindowAddsEntryWithCorrectTimerDurationTest(){
        int windowSize = 5;
        int timerDuration = 10;
        Window window = new BasicWindow(windowSize, timerDuration);
        Packet packet = new Packet.PacketBuilder().build();
        window.add(packet);
        ArrayBlockingQueue<WindowEntry> queue = (ArrayBlockingQueue) window;
        Assert.assertEquals(timerDuration, queue.poll().getPacketTimeout().getTimeoutValue());
    }


    @Test
    public void windowSizeReturnsCorrectWindowSizeTest(){
        int windowSize = 5;
        Window window = new BasicWindow(windowSize, 1);
        Assert.assertEquals(windowSize, window.windowSize());
    }

    @Test
    public void windowSizeReturnsCorrectWindowSize100TimesTest(){
        for (int i = 1; i < 100; i++) {
            Window window = new BasicWindow(i, 1);
            Assert.assertEquals(i, window.windowSize());
        }
    }




}
