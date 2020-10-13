package org.example.network;

public class Channel {

    private NetworkNode source;
    private NetworkNode destination;
    private int cost;

    public Channel(NetworkNode source, NetworkNode destination, int cost) {
        this.source = source;
        this.destination = destination;
        this.cost = cost;
    }


    public NetworkNode getSource() {
        return source;
    }

    public NetworkNode getDestination() {
        return destination;
    }

    public int getCost() {
        return cost;
    }
}
