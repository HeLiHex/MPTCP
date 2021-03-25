package org.example.network.address;

import java.util.UUID;

public class UUIDAddress extends Address {

    private final UUID id;

    public UUIDAddress() {
        this.id = UUID.randomUUID();
    }

    @Override
    public String getId() {
        return this.id.toString();
    }

}
