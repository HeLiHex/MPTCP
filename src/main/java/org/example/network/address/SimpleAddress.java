package org.example.network.address;

public class SimpleAddress extends Address{

    private final String id;

    public SimpleAddress(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
