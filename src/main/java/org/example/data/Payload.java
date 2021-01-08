package org.example.data;

public interface Payload {

    /**
     * A method that returns the size of the packet.
     * The size of an object is abstract and must be implemented according to what type
     * Payload is created.
     *
     * @return the size of the packet
     */
    int size();

    /**
     * A method that returns a String representation of the Payload
     *
     * @return String representation.
     */
    String toString();

    /**
     * A method to check if two object is equal or not
     *
     * @param obj to be checked against
     * @return true if this is considered equal to given object
     */
    boolean equals(Object obj);

    /**
     * A method that creates a hash number of this object
     *
     * @return Integer hash of the object
     */
    int hashCode();

}
