package org.example.protocol;

import org.example.Client;
import org.example.network.NetworkNode;
import org.example.network.Router;
import org.example.protocol.util.BufferQueue;
import org.example.protocol.util.Packet;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbstractTCP extends Router implements TCP{

    private Logger logger = Logger.getLogger(Client.class.getName());

    private Queue<Packet> inputBuffer;
    private Queue<Packet> outputBuffer;

    public AbstractTCP(int inputBufferSize, int outputBufferSize) {
        super();
        this.outputBuffer = new BufferQueue<>(outputBufferSize);
        this.inputBuffer = new BufferQueue<>(inputBufferSize);
    }

    @Override
    public void connect() {

    }

    @Override
    public void send(Packet packet) {
        boolean wasAdded = this.outputBuffer.offer(packet);
        if (!wasAdded){
            logger.log(Level.WARNING, "packet was not added to the output queue");
            return;
        }
    }

    public Packet trySend(){
        if (this.outputBuffer.isEmpty()) return null;

        Packet packet = this.outputBuffer.poll();
        NetworkNode pathNode = this.getPath(packet.getDestination());

        while (this.inputBuffer.isEmpty()){
            //wait until an ACK is received
            System.out.println("waiting");
        }

        if (!this.inputBuffer.peek().getMsg().equals("ACK")){
            this.logger.log(Level.FINE, "msg was not ACK");
            return null;
        }
        return this.inputBuffer.poll();
    }

    @Override
    public Packet receive() {
        return this.inputBuffer.remove();
    }

    @Override
    public void close() {

    }

    public Queue<Packet> getOutputBuffer() {
        return this.outputBuffer;
    }
}
