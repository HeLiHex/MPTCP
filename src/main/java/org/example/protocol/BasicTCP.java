package org.example.protocol;

import org.example.protocol.util.BufferQueue;

public class BasicTCP extends AbstractTCP {

    private static final int BUFFER_SIZE = 20;

    public BasicTCP() {
        super(BUFFER_SIZE, BUFFER_SIZE);
    }


}
