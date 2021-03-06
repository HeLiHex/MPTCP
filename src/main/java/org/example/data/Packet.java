package org.example.data;

import org.example.network.interfaces.Endpoint;

import java.util.List;

public class Packet {
    private final Endpoint destination;
    private final Endpoint origin;
    private final List<Flag> flags;
    private final Payload payload;
    private final int index;

    private final int sequenceNumber;
    private final int acknowledgmentNumber;


    protected Packet(Endpoint destination, Endpoint origin, List<Flag> flags, Payload payload, int sequenceNumber, int acknowledgmentNumber, int index) {
        this.destination = destination;
        this.origin = origin;
        this.flags = flags;
        this.payload = payload;
        this.index = index;

        this.sequenceNumber = sequenceNumber;
        this.acknowledgmentNumber = acknowledgmentNumber;
    }

    public boolean hasAllFlags(Flag... flags) {
        if (flags == null || this.flags == null) {
            throw new NullPointerException("flags can't be null");
        }

        if (this.flags.isEmpty()) {
            // flags given are not in the list
            return false;
        }

        if (flags.length == 0) {
            // no flags given implies that all flags given are in list
            return true;
        }

        boolean hasFlag = this.flags.contains(flags[0]);
        for (var i = 1; i < flags.length; i++) {
            hasFlag &= this.flags.contains(flags[i]);
        }
        return hasFlag;
    }

    public Payload getPayload() {
        return payload;
    }

    public Endpoint getDestination() {
        return this.destination;
    }

    public Endpoint getOrigin() {
        return this.origin;
    }


    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public int getIndex() {
        return this.index;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public int size() {
        if (this.payload == null) return 0;
        return payload.size();
    }

    protected List<Flag> getFlags() {
        return flags;
    }

    @Override
    public String toString() {
        String returnString;
        if (this.payload == null) {
            returnString = this.flags.toString();
        } else {
            returnString = "[" + this.payload.toString() + "]";
        }
        returnString += "[seq: " + this.getSequenceNumber() + "]" + "[index: " + this.getIndex() + "]";

        return returnString;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var packet = (Packet) o;
        return sequenceNumber == packet.sequenceNumber
                && acknowledgmentNumber == packet.acknowledgmentNumber
                && destination.equals(packet.destination)
                && origin.equals(packet.origin)
                && packet.hasAllFlags(this.flags.toArray(Flag[]::new));
    }
}


