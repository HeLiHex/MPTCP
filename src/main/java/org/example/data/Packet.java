package org.example.data;

import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.Connection;

import java.util.ArrayList;
import java.util.List;

public class Packet {
    public static class PacketBuilder {
        private Endpoint destination = null;
        private Endpoint origin = null;
        private List<Flag> flags = new ArrayList<>();
        private Payload payload = null;

        private int sequenceNumber = -1;
        private int acknowledgmentNumber = -1;

        public Packet build(){
            if (!this.hasFlag(Flag.ACK)){
                this.acknowledgmentNumber = -1;
            }
            return new Packet(this.destination, this.origin, this.flags, this.payload, this.sequenceNumber, this.acknowledgmentNumber);
        }

        public PacketBuilder withFlags(Flag... flags){
            for (Flag flag : flags) {
                if (this.flags.contains(flag)) continue;
                this.flags.add(flag);
            }
            return this;
        }

        public PacketBuilder withPayload(Payload payload){
            this.payload = payload;
            return this;
        }

        public PacketBuilder withOrigin(Endpoint self){
            this.origin = self;
            return this;
        }

        public PacketBuilder withDestination(Endpoint destination){
            this.destination = destination;
            return this;
        }

        public PacketBuilder withSequenceNumber(int sequenceNumber){
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public PacketBuilder withAcknowledgmentNumber(int acknowledgmentNumber){
            this.acknowledgmentNumber = acknowledgmentNumber;
            return this;
        }

        public PacketBuilder withConnection(Connection connection){
            this.withOrigin(connection.getConnectionSource());
            this.withDestination(connection.getConnectedNode());
            this.withSequenceNumber(connection.getNextSequenceNumber());
            this.withAcknowledgmentNumber(connection.getNextAcknowledgementNumber());
            return this;
        }

        public boolean hasFlag(Flag... flags){
            boolean hasFlag = true;
            for (Flag flag : flags) {
                hasFlag &= this.flags.contains(flag);
            }
            return hasFlag;
        }

    }

    private Endpoint destination;
    private Endpoint origin;
    private List<Flag> flags;
    private Payload payload;

    private int sequenceNumber;
    private int acknowledgmentNumber;


    private Packet(Endpoint destination, Endpoint origin, List<Flag> flags, Payload payload, int sequenceNumber, int acknowledgmentNumber) {
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

    @Override
    public String toString() {
        if (this.payload == null) return this.flags.toString();
        return "[" + this.payload.toString() + "]";
    }
}


