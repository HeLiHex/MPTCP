package org.example.data;

import org.example.network.NetworkNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class Packet {
    public static class PacketBuilder {

        private NetworkNode destination = null;
        private List<Flag> flags = new ArrayList<>();
        private String msg = null;

        public Packet build(){
            return new Packet(destination, flags, msg);
        }

        public PacketBuilder withFlag(Flag flag){
            if (this.flags.contains(flag)) return this;
            this.flags.add(flag);
            return this;
        }

        public PacketBuilder withMsg(String msg){
            this.msg = msg;
            return this;

        }

        public PacketBuilder withDestination(NetworkNode destination){
            this.destination = destination;
            return this;
        }

    }


    private NetworkNode destination;
    private List<Flag> flags;
    private String msg;


    public Packet(NetworkNode destination, List<Flag> flags, String msg) {
        this.destination = destination;
        this.flags = flags;
        this.msg = msg;
    }

    //tmp
    public String getMsg() {
        return msg;
    }


    public NetworkNode getDestination() {
        if (this.destination == null) System.out.println("This packet has no destination");
        return this.destination;
    }

    @Override
    public String toString() {
        return "["+getMsg()+"]";
    }
}


