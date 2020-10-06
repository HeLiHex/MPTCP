package org.example.protocol;

import org.example.data.Packet;

public interface TCP {


    /**
     * 1. send SYN with random number A
     * 2. receive SYN-ACK with A+1 and sequence number random B (the server chooses B)
     * 3. send ACK back to server. The ACK is now A+2 and sequence number is B+1
     */
    void connect();

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
