package org.example.protocol.util;

public class Packet {

    private String sourcePort; //todo mulig disse bare skal være represnetert gjennom Address
    private int destinationPort;
    private int sequenceNumber;
    private int ackNumber;
    private int windowSize;
    private String checksum;

    //tmp
    private String msg;

    //tmp
    public Packet(String msg) {
        this.msg = msg;
    }

    //tmp
    public String getMsg() {
        return msg;
    }
}
