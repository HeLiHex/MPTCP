package org.example.protocol;

import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;

import java.util.List;

public interface TCP extends Endpoint {

    /**
     * A method that initiates connection with a host
     * <p>
     * 1. Send SYN with random number A
     * 2. Receive SYN-ACK with A+1 and sequence number random B (the server chooses B)
     * 3. Send ACK back to server. The ACK is now A+2 and sequence number is B+1
     *
     * @param host to connect to
     */
    void connect(TCP host);

    /**
     * A method that handles incoming connections
     * <p>
     * 1. Receive SYN Packet with random number A that indicates that a client wants to connect
     * 2. Send SYN-ACK with A+1 and sequence number random B
     * 3. Receive ACK with ACK-number A+2 and sequence number is B+1
     *
     * @param syn Packet to start incoming connection from a client
     */
    void connect(Packet syn);

    /**
     * Creates a packet with Payload and enqueues the new Packet to the output-buffer
     *
     * @param payload
     */
    void send(Payload payload);

    /**
     * Dequeues the Packet from the received queue
     * All packets in the received queue are acknowledged and in correct order
     *
     * @return next Packet
     */
    Packet receive();


    /**
     * A method that to determine if TCP has an open connection
     *
     * @return true if TCP has an active connection
     */
    boolean isConnected();

    /**
     * A method that returns the available channel to route packets through
     *
     * @return an available Channel
     */
    Channel getChannel();

    /**
     * A method that returns the retransmission timeout value
     *
     * @return the RTO for this connection
     */
    long getRTO();

    /**
     * A method to handle the incoming packets
     *
     * @return returns true if a packet should be ACKed
     */
    boolean handleIncoming();

    /**
     * A method to handle the sending process of TCP
     *
     * @return a List of the sent packets
     */
    List<Packet> trySend();

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
     * A method that returns the main flow or controller of this TCP
     *
     * @return the main TCP flow
     */
    TCP getMainFlow();

    /**
     * A method that retuns the number of subflows available
     *
     * @return the number of subflows
     */
    int getNumberOfFlows();


}
