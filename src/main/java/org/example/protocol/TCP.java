package org.example.protocol;

import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;

public interface TCP{

    /**
     * 1. Send SYN with random number A
     * 2. Receive SYN-ACK with A+1 and sequence number random B (the server chooses B)
     * 3. Send ACK back to server. The ACK is now A+2 and sequence number is B+1
     * @param host to connect to
     */
    void connect(Endpoint host);

    /**
     * 1. Receive SYN Packet with random number A that indicates that a client wants to connect
     * 2. Send SYN-ACK with A+1 and sequence number random B
     * 3. Receive ACK with ACK-number A+2 and sequence number is B+1
     * @param syn Packet to start incoming connection from a client
     */
    void connect(Packet syn);

    /**
     * Enqueues a new Packet to the output-buffer
     * @param packet
     */
    void send(Packet packet);

    /**
     * Dequeues the Packet from the input-buffer
     * @return Packet
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




}
