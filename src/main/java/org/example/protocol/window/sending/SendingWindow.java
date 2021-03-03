package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.util.BoundedQueue;

public interface SendingWindow extends BoundedQueue<Packet> {


    boolean isWaitingForAck();

    @Override
    boolean offer(Packet packet);

    @Override
    Packet poll();

    @Override
    boolean isEmpty();

}
