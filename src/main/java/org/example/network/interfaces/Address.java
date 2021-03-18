package org.example.network.interfaces;

public interface Address {

    String getIdentifier();

    boolean equals(Object o);

    int hashCode();
}
