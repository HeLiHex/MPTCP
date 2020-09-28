package org.example.network;

import java.util.List;

public interface NetworkNode {

    public Address getAddress();

    public List<NetworkNode> getNeighbours();

    public void addNeighbour(NetworkNode node);

    public int getCost();

    public NetworkNode getPath(NetworkNode destination);

    void updateRoutingTable();

}
