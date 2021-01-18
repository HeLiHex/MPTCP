package org.example.util;

import org.example.data.PacketBuilder;
import org.junit.Test;

import java.time.Duration;

public class WaitingPacketTest {

    @Test(expected = IllegalStateException.class)
    public void restartUnfinishedTimerThrowsException(){
        WaitingPacket wp = new WaitingPacket(new PacketBuilder().build(), Duration.ofSeconds(5));
        wp.restart();
    }

}
