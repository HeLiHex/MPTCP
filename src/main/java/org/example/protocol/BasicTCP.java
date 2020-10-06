package org.example.protocol;

import org.example.data.BufferQueue;
import org.example.data.Packet;

import java.util.Random;

public class BasicTCP extends AbstractTCP {

    private static final int BUFFER_SIZE = 20;

    public BasicTCP(Random randomGenerator) {
        super(new BufferQueue<Packet>(BUFFER_SIZE), new BufferQueue<Packet>(BUFFER_SIZE), randomGenerator);
    }


}
