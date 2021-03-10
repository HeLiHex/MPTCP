package org.example.protocol.window;


public interface IWindow {

    boolean inWindow(int packetIndex);

    int getWindowCapacity();

}
