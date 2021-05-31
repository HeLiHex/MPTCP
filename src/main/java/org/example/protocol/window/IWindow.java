package org.example.protocol.window;


public interface IWindow {

    /**
     * A method that uses the given packet-index to determine
     * if the packet is inside the window
     *
     * @param packetIndex
     * @return true if the packet is in the window
     */
    boolean inWindow(int packetIndex);

    /**
     * A method that returns the window capacity
     *
     * @return the window capacity
     */
    int getWindowCapacity();

}
