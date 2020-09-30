package org.example.network;

import java.util.UUID;

public class Address implements Comparable<Address>{

    private final UUID address;

    public Address() {
        this.address = UUID.randomUUID();
    }

    public UUID getAddress(){
        return this.address;
    }

    @Override
    public String toString() {
        return this.address.toString();
    }

    @Override
    public int compareTo(Address address) {
        return this.address.compareTo(address.getAddress());
    }

    @Override
    public boolean equals(Object obj) {
        return this.address.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.address.hashCode();
    }
}
