package org.example.protocol;

import org.example.data.Flag;
import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;


import java.util.Objects;

public class Connection {

    private Endpoint self;
    private Endpoint other;
    private int sequenceNumber;
    private int acknowledgementNumber;

    private final int CONSTANT;


    public Connection(Endpoint self, Endpoint other, int sequenceNumber, int acknowledgementNumber) {
        this.self = self;
        this.other = other;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgementNumber = acknowledgementNumber;
        this.CONSTANT = sequenceNumber;
    }

    public void update(Packet packet){
        if (packet.hasAllFlags(Flag.ACK)){
            this.sequenceNumber = packet.getAcknowledgmentNumber();
            return;
        }
        this.acknowledgementNumber = packet.getSequenceNumber() + 1;
    }


    public int getNextSequenceNumber() {
        return sequenceNumber;
    }

    public int getNextAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public Endpoint getConnectedNode() {
        return other;
    }

    public Endpoint getConnectionSource() {
        return self;
    }

    public int getCONSTANT() {
        return CONSTANT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(self, that.self) &&
                Objects.equals(other, that.other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, other);
    }

    @Override
    public String toString() {
        return "Connection[" + other + "]";
    }
}
