package org.example.protocol;

import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;

public class BasicTCP extends AbstractTCP {

    private static final int BUFFER_SIZE = 20;
    private static final int SEED = 10;

    public BasicTCP() {
        super(new BufferQueue<Packet>(BUFFER_SIZE), new BufferQueue<Packet>(BUFFER_SIZE), SEED);
    }


}
