package org.example.protocol.util;

import org.example.network.NetworkNode;

import java.util.NoSuchElementException;

public class Packet {

    private String sourcePort; //todo mulig disse bare skal være represnetert gjennom Address
    private int destinationPort;
    private int sequenceNumber;
    private int ackNumber;
    private int windowSize;
    private String checksum;


    private NetworkNode destination;
    private String msg;


    public Packet(String msg) {
        this.msg = msg;
    }

    public Packet(String msg, NetworkNode destination) {
        this.msg = msg;
        this.destination = destination;
    }

    //tmp
    public String getMsg() {
        return msg;
    }



    public NetworkNode getDestination() {
        if (this.destination == null) System.out.println("This packet has no destination");
        return this.destination;
    }

    public void setDestination(NetworkNode destination){
        this.destination = destination;
    }


    @Override
    public String toString() {
        return "["+getMsg()+"]";
    }
}
