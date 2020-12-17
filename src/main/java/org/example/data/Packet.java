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

    public boolean hasAllFlags(Flag... flags){
        boolean hasFlag = this.flags.contains(flags[0]);
        for (int i = 1; i < flags.length; i++) {
            hasFlag &= this.flags.contains(flags[i]);
        }
        return hasFlag;
    }

    public boolean hasOneOfFlags(Flag... flags){
        boolean hasFlag = this.flags.contains(flags[0]);
        for (int i = 1; i < flags.length; i++) {
            hasFlag |= this.flags.contains(flags[i]);
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


    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public void setAcknowledgmentNumber(int acknowledgmentNumber) {
        this.acknowledgmentNumber = acknowledgmentNumber;
    }

    public int size(){
        if (this.payload == null) return 0;
        return payload.size();
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
}


