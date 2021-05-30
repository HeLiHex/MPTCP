package org.example.data;

import org.example.network.interfaces.Endpoint;
import org.example.protocol.Connection;

import java.util.ArrayList;
import java.util.List;


public class PacketBuilder {
    private Endpoint destination = null;
    private Endpoint origin = null;
    private List<Flag> flags = new ArrayList<>();
    private Payload payload = null;
    private int index = 0;

    private int sequenceNumber = -1;
    private int acknowledgmentNumber = -1;

    public Packet build() {
        if (!this.hasFlag(Flag.ACK)) {
            this.acknowledgmentNumber = -1;
        }
        return new Packet(this.destination, this.origin, this.flags, this.payload, this.sequenceNumber, this.acknowledgmentNumber, this.index);
    }

    public Packet ackBuild(Packet packetToAck) {
        if (packetToAck.getOrigin() == null) throw new IllegalArgumentException("no origin");
        if (packetToAck.getDestination() == null) throw new IllegalArgumentException("no destination");

        this.withDestination(packetToAck.getOrigin());
        this.withOrigin(packetToAck.getDestination());
        this.withFlags(Flag.ACK);
        this.withPayload(null);
        this.withAcknowledgmentNumber(packetToAck.getSequenceNumber() + 1);
        this.withSequenceNumber(packetToAck.getSequenceNumber());
        this.withIndex(packetToAck.getIndex());
        return new Packet(this.destination, this.origin, this.flags, this.payload, this.sequenceNumber, this.acknowledgmentNumber, this.index);
    }

    public PacketBuilder withFlags(Flag... flags) {
        for (Flag flag : flags) {
            if (this.flags.contains(flag)) continue;
            this.flags.add(flag);
        }
        return this;
    }

    public PacketBuilder withPayload(Payload payload) {
        this.payload = payload;
        return this;
    }

    public PacketBuilder withOrigin(Endpoint self) {
        this.origin = self;
        return this;
    }

    public PacketBuilder withDestination(Endpoint destination) {
        this.destination = destination;
        return this;
    }

    public PacketBuilder withSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public PacketBuilder withAcknowledgmentNumber(int acknowledgmentNumber) {
        this.acknowledgmentNumber = acknowledgmentNumber;
        return this;
    }

    public PacketBuilder withConnection(Connection connection) {
        this.withOrigin(connection.getConnectionSource());
        this.withDestination(connection.getConnectedNode());
        return this;
    }

    public PacketBuilder withIndex(int index) {
        this.index = index;
        return this;
    }


    public boolean hasFlag(Flag... flags) {
        var hasFlag = true;
        for (Flag flag : flags) {
            hasFlag &= this.flags.contains(flag);
        }
        return hasFlag;
    }

}


