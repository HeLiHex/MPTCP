package org.example.network;

import java.util.UUID;

public class Address {

    private final String address;

    public Address() {
        this.address = UUID.randomUUID().toString();
    }

    public String getAddress() {
        return address;
    }
}
