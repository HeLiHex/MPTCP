package org.example.network;

import org.example.data.Packet;

public interface Endpoint extends NetworkNode{

    Packet dequeueOutputBuffer();


    boolean enqueueOutputBuffer(Packet packet);


    boolean outputBufferIsEmpty();
}
