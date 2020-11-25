package org.example.protocol.window;

import org.example.data.Packet;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

public class WindowTest {



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
