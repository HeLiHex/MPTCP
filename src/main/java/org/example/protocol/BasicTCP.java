package org.example.protocol;

import org.example.data.BufferQueue;
import org.example.data.Packet;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicTCP extends AbstractTCP {

    private final Logger logger = Logger.getLogger(BasicTCP.class.getName());

    private static final int BUFFER_SIZE = 20;
    private static final double NOISE_TOLERANCE = 100.0;
    private boolean waitingForACK;
    private Connection connection;

    public BasicTCP(Random randomGenerator) {
        super(new BufferQueue<Packet>(BUFFER_SIZE), new BufferQueue<Packet>(BUFFER_SIZE), randomGenerator, NOISE_TOLERANCE);
        this.waitingForACK = false;
    }

    @Override
    protected Connection getConnection() {
        if (this.connection == null) logger.log(Level.WARNING, "no connection established!");
        return this.connection;
    }

    @Override
    protected void updateConnection(Packet packet){
        this.connection.update(packet);
    }

    @Override
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected void closeConnection() {
        if (this.connection == null){
            logger.log(Level.WARNING, "There is noe connection to be closed");
            return;
        }
        logger.log(Level.INFO, () -> "Connection to " + this.connection.getConnectedNode() + " is closed");
        this.connection = null;
    }

    @Override
    protected boolean isWaitingForACK() {
        return this.waitingForACK;
    }

    @Override
    protected void releaseWaitForAck() {
        this.waitingForACK = false;
    }

    @Override
    protected void setWaitForAck() {
        this.waitingForACK = true;
    }

    @Override
    protected boolean packetIsFromValidConnection(Packet packet) {
        Connection conn = this.connection;
        if (conn == null) return false;
        return packet.getSequenceNumber() == conn.getNextAcknowledgementNumber()
                && packet.getOrigin().equals(conn.getConnectedNode())
                && packet.getDestination().equals(conn.getConnectionSource()
        );
    }
}
