package org.example.protocol;

import org.example.protocol.util.Packet;
import org.junit.Assert;
import org.junit.Test;

public class BasicTCPTest {


    @Test
    public void sendEnqueuesPackageToOutputBufferTest(){
        TCP tcp = new BasicTCP();
        String msg = "test";
        tcp.send(new Packet(msg));

        Packet getPacketInBuffer = ((BasicTCP)tcp).getOutputBuffer().peek();
        Assert.assertEquals(getPacketInBuffer.getMsg(), msg);

    }


}
