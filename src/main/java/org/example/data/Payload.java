package org.example.data;

public interface Payload {

    int size();

    @Override
    String toString();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

}
