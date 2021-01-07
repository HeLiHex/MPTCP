package org.example.util;

import org.example.data.PacketBuilder;
import org.junit.Test;

public class WaitingPacketTest {

    @Test(expected = IllegalStateException.class)
    public void restartUnfinishedTimerThrowsException(){
        WaitingPacket wp = new WaitingPacket(new PacketBuilder().build(), 10);
        wp.restart();
    }

}
