package org.example.protocol;

public class Packet {

    private String sourcePort; //todo mulig disse bare skal være represnetert gjennom Address
    private int destinationPort;
    private int sequenceNumber;
    private int ackNumber;
    private int windowSize;
    private String checksum;

}
