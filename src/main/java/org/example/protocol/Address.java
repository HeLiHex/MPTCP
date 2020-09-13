package org.example.protocol;

public class Address {

    private final int port;
    private final String address;

    public Address(int port, String address) {
        this.port = port;
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
