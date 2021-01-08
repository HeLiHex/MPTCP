package org.example.data;

import org.example.network.interfaces.Endpoint;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Packet {

    private final Logger logger;

    private Endpoint destination;
    private Endpoint origin;
    private List<Flag> flags;
    private Payload payload;

    private int sequenceNumber;
    private int acknowledgmentNumber;


    public Packet(Endpoint destination, Endpoint origin, List<Flag> flags, Payload payload, int sequenceNumber, int acknowledgmentNumber) {
        this.logger = Logger.getLogger(this.getClass().getName());

        this.destination = destination;
        this.origin = origin;
        this.flags = flags;
        this.payload = payload;

        this.sequenceNumber = sequenceNumber;
        this.acknowledgmentNumber = acknowledgmentNumber;
    }

    public boolean hasAllFlags(Flag... flags){
        boolean hasFlag = this.flags.contains(flags[0]);
        for (int i = 1; i < flags.length; i++) {
            hasFlag &= this.flags.contains(flags[i]);
        }
        return hasFlag;
    }

    public Payload getPayload() {
        return payload;
    }

    public Endpoint getDestination() {
        if (this.destination == null) logger.log(Level.WARNING, "This packet has no destination");
        return this.destination;
    }

    public Endpoint getOrigin() {
        return this.origin;
    }


    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public int size(){
        if (this.payload == null) return 0;
        return payload.size();
    }

    protected List<Flag> getFlags() {
        return flags;
    }

    @Override
    public String toString() {
        String returnString;
        if (this.payload == null){
            returnString = this.flags.toString();
        }
        else{
            returnString = "[" + this.payload.toString() + "]";
        }
        returnString += "[seq: " + this.getSequenceNumber() + "]";

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
        Packet packet = (Packet) o;
        return sequenceNumber == packet.sequenceNumber
                && acknowledgmentNumber == packet.acknowledgmentNumber
                && destination.equals(packet.destination)
                && origin.equals(packet.origin)
                && packet.hasAllFlags((Flag[])this.flags.stream().toArray());
    }
}


