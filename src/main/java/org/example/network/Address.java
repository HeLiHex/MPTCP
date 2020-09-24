package org.example.network;

public class Address {

    private final String address;
    private final int port;

    public Address(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
