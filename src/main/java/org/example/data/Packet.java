package org.example.data;

import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.Connection;

import java.util.ArrayList;
import java.util.List;

public class Packet {

    private Endpoint destination;
    private Endpoint origin;
    private List<Flag> flags;
    private Payload payload;

    private int sequenceNumber;
    private int acknowledgmentNumber;


    public Packet(Endpoint destination, Endpoint origin, List<Flag> flags, Payload payload, int sequenceNumber, int acknowledgmentNumber) {
        this.destination = destination;
        this.origin = origin;
        this.flags = flags;
        this.payload = payload;

        this.sequenceNumber = sequenceNumber;
        this.acknowledgmentNumber = acknowledgmentNumber;
    }

    public boolean hasFlag(Flag... flags){
        boolean hasFlag = true;
        for (Flag flag : flags) {
            hasFlag &= this.flags.contains(flag);
        }
        return hasFlag;
    }

    public Payload getPayload() {
        return payload;
    }

    public Endpoint getDestination() {
        if (this.destination == null) System.out.println("This packet has no destination");
        return this.destination;
    }

    public Endpoint getOrigin() {
        return this.origin;
    }

    //public void setOrigin(Endpoint origin) {
        //this.origin = origin;
    //}

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public int size(){
        if (this.payload == null) return 0;
        return payload.size();
    }

    @Override
    public String toString() {
        if (this.payload == null) return this.flags.toString();
        return "[" + this.payload.toString() + "]";
    }
}


