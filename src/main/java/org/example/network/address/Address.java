package org.example.network.address;

import java.util.Objects;

public abstract class Address {

    public abstract String getId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var address = (Address) o;
        return Objects.equals(this.getId(), address.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return this.getId();
    }

}
