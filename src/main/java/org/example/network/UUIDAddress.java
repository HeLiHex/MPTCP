package org.example.network;

import org.example.network.interfaces.Address;

import java.util.Objects;
import java.util.UUID;

public class UUIDAddress implements Address {

    private final UUID identifier;

    public UUIDAddress() {
        this.identifier = UUID.randomUUID();
    }

    @Override
    public String getIdentifier() {
        return this.identifier.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(identifier, address.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
