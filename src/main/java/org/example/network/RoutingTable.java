package org.example.network;

import java.util.ArrayList;
import java.util.List;

public class RoutingTable {

    private List<List> table;
    private NetworkNode networkNode;

    public RoutingTable(NetworkNode networkNode) {
        this.networkNode = networkNode;
        this.table = new ArrayList<List>(2);
        this.table.add(new ArrayList<NetworkNode>());
        this.table.add(new ArrayList<Integer>());
        this.table.add(new ArrayList<NetworkNode>());
        addEntry(this.networkNode, 0, null);
    }

    private void updateTable(NetworkNode node, int cost, NetworkNode comingFrom){
        if (this.contains(node)){
            updateEntry(node, cost, comingFrom);
            return;
        }
        addEntry(node, cost, comingFrom);
    }

    private void addEntry(NetworkNode nodeToAdd, int cost, NetworkNode comingFrom){
        List<NetworkNode> nodes = this.table.get(0);
        List<Integer> costs = this.table.get(1);
        List<NetworkNode> prevNodes = this.table.get(2);
        nodes.add(nodeToAdd);
        costs.add(cost);
        prevNodes.add(comingFrom);
    }

    private void updateEntry(NetworkNode nodeToUpdate, int cost, NetworkNode comingFrom){
        int index = this.table.get(0).indexOf(nodeToUpdate);
        List<Integer> costs = this.table.get(1);
        List<NetworkNode> prevNodes = this.table.get(2);

        if (costs.get(index) > cost){
            System.out.println("update");
            costs.set(index, cost);
            prevNodes.set(index, comingFrom);
        }
    }

    private boolean contains(NetworkNode node){
        List<NetworkNode> nodes = this.table.get(0);
        return nodes.contains(node);
    }

    //todo -- funker ikke
    public NetworkNode getPath(NetworkNode destination){
        int index = this.table.get(0).indexOf(destination);
        if (index < 0){
            System.out.println("this destination does not exist");
            return null;
        }

        NetworkNode prevNode = (NetworkNode) this.table.get(2).get(index);
        return prevNode.getPath(destination);
    }


    public void update(NetworkNode staringNode){
        update(staringNode, new ArrayList<NetworkNode>(), 0);
    }

    //Dijstra
    public void update(NetworkNode staringNode, List<NetworkNode> visited, int cost){
        if (visited.contains(staringNode)){
            System.out.println("table updated!");
            return;
        }

        List<NetworkNode> neighbourList = staringNode.getNeighbours();
        NetworkNode bestNode = neighbourList.get(0);

        if(bestNode == null){
            System.out.println("this node has no neighbours");
            return;
        }
        this.updateTable(bestNode, cost + bestNode.getCost(), staringNode);

        for (int i = 1; i < staringNode.getNeighbours().size(); i++) {
            NetworkNode nextNode = neighbourList.get(i);
            this.updateTable(nextNode, cost + nextNode.getCost(), staringNode);
            if(bestNode.getCost() > nextNode.getCost()){
                bestNode = nextNode;
            }
        }

        visited.add(bestNode);
        update(bestNode, visited, bestNode.getCost() + cost);
    }


    @Override
    public String toString() {
        String returnString = "";
        for (int i = 0; i < table.get(0).size(); i++) {
            returnString += "Destination: " + table.get(0).get(i) + " | Cost: " + table.get(1).get(i) + " | Previous: " + table.get(2).get(i) + "\n";
        }
        return returnString;
    }
}
