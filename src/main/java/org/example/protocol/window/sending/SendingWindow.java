package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.protocol.window.IWindow;
import org.example.simulator.statistics.TCPStats;
import org.example.util.BoundedQueue;

public interface SendingWindow extends IWindow, BoundedQueue<Packet> {

    /**
     * A method that checks if the sendigwindow should wait for ACK or not
     *
     * @return true if the sendingwindow should wait
     */
    boolean isWaitingForAck();

    /**
     * A method that handles received ACKs
     *
     * @param ack
     */
    void ackReceived(Packet ack);

    /**
     * A method that returns the packet that should be sent
     *
     * @return packet to be sent
     */
    Packet send();

    /**
     * A method that checks if a packet should be retransmitted or not
     *
     * @param packet that should be checked for possible retransmission
     * @return true if the packet should be retransmitted
     */
    boolean canRetransmit(Packet packet);

    /**
     * A method that returns a packet that should be fast retransmitted
     *
     * @return Packet if there is a packet to fast-retransmit, else null
     */
    Packet fastRetransmit();

    /**
     * A method that is called to increase the size of the sendigwindow.
     */
    void increase();

    /**
     * A method that is called to decrease the size of the sendigwindow.
     */
    void decrease();

    /**
     * A method that calculates the sending window packet index
     * of the given packet
     *
     * @param packet to calculate packet index of
     * @return the packet index of the given packet
     */
    int sendingPacketIndex(Packet packet);

    /**
     * A method that returns the connection
     *
     * @return connection
     */
    Connection getConnection();

    /**
     * A method that checks if the sending window is empty or not
     *
     * @return true if the sending window is empty
     */
    boolean isQueueEmpty();

    /**
     * A method that returns the stats of the sending window
     *
     * @return the TCPStats
     */
    TCPStats getStats();

}
