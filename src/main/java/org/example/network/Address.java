package org.example.network;

import java.util.UUID;

public class Address implements Comparable<Address> {

    private final UUID identifier;

    public Address() {
        this.identifier = UUID.randomUUID();
    }

    private UUID getIdentifier() {
        return this.identifier;
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }


    @Override
    public int compareTo(Address address) {
        return this.identifier.compareTo(address.getIdentifier());
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Address) return this.identifier.hashCode() == obj.hashCode();
        return false;
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }
}
