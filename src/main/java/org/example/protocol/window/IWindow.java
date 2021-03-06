package org.example.protocol.window;

import org.example.data.Packet;

public interface IWindow {

    int sendingPacketIndex(Packet packet);

    int receivingPacketIndex(Packet packet);

    boolean inSendingWindow(Packet packet);

    boolean inReceivingWindow(Packet packet);

    int getWindowSize();
}
