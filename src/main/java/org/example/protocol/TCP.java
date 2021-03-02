package org.example.protocol;

import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;

public interface TCP {

    /**
     * A method that initiates connection with a host
     * <p>
     * 1. Send SYN with random number A
     * 2. Receive SYN-ACK with A+1 and sequence number random B (the server chooses B)
     * 3. Send ACK back to server. The ACK is now A+2 and sequence number is B+1
     *
     * @param host to connect to
     */
    void connect(Endpoint host);

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
     * Enqueues a new Packet to the output-buffer
     *
     * @param packet
     */
    void send(Packet packet);

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
     * both endpoints can use terminate the connection
     * 1. send FIN
     * 2. receive ACK
     * 3. the other endpoint does the same
     * 4. the first endpoint to send FIN waits for a timeout before finally closing connection
     */
    void close();


    /**
     * A method that to determine if TCP has an open connection
     *
     * @return true if TCP has an active connection
     */
    boolean isConnected();

    Channel getChannel();

    Endpoint getConnectedEndpoint();

    long getRTT();

    long getRTO();


}
