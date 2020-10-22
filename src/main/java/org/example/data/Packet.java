package org.example.data;

import org.example.network.interfaces.NetworkNode;

import java.util.ArrayList;
import java.util.List;

public class Packet {
    public static class PacketBuilder {
        private NetworkNode destination = null;
        private NetworkNode origin = null;
        private List<Flag> flags = new ArrayList<>();
        private String msg = null;

        private int sequenceNumber;
        private int acknowledgmentNumber;

        public Packet build(){
            return new Packet(this.destination, this.origin, this.flags, this.msg, this.sequenceNumber, this.acknowledgmentNumber);
        }

        public PacketBuilder withFlags(Flag... flags){
            for (Flag flag : flags) {
                if (this.flags.contains(flag)) continue;
                this.flags.add(flag);
            }
            return this;
        }

        public PacketBuilder withMsg(String msg){
            this.msg = msg;
            return this;
        }

        public PacketBuilder withOrigin(NetworkNode self){
            this.origin = self;
            return this;
        }

        public PacketBuilder withDestination(NetworkNode destination){
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
    }

    private NetworkNode destination;
    private NetworkNode origin;
    private List<Flag> flags;
    private String msg;

    private int sequenceNumber;
    private int acknowledgmentNumber;


    private Packet(NetworkNode destination, NetworkNode origin, List<Flag> flags, String msg, int sequenceNumber, int acknowledgmentNumber) {
        this.destination = destination;
        this.origin = origin;
        this.flags = flags;
        this.msg = msg;

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

    public String getMsg() {
        return msg;
    }


    public NetworkNode getDestination() {
        if (this.destination == null) System.out.println("This packet has no destination");
        return this.destination;
    }

    public NetworkNode getOrigin() {
        return this.origin;
    }

    public void setOrigin(NetworkNode origin) {
        this.origin = origin;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    @Override
    public String toString() {
        if (this.getMsg() == null) return this.flags.toString();
        return "["+getMsg()+"]";
    }
}


