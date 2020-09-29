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

    private void updateTable(NetworkNode node, int cost, NetworkNode comingFrom) {
        if (this.contains(node)) {
            updateEntry(node, cost, comingFrom);
            return;
        }
        addEntry(node, cost, comingFrom);
    }

    private void addEntry(NetworkNode nodeToAdd, int cost, NetworkNode comingFrom) {
        List<NetworkNode> nodes = this.getDestinationNodes();
        List<Integer> costs = this.getCosts();
        List<NetworkNode> prevNodes = this.getPreviousNodes();
        nodes.add(nodeToAdd);
        costs.add(cost);
        prevNodes.add(comingFrom);
    }

    private void updateEntry(NetworkNode nodeToUpdate, int cost, NetworkNode comingFrom) {
        int index = this.getDestinationNodes().indexOf(nodeToUpdate);
        List<Integer> costs = this.getCosts();
        List<NetworkNode> prevNodes = this.getPreviousNodes();

        if (costs.get(index) > cost) {
            System.out.println("update entry");
            costs.set(index, cost);
            prevNodes.set(index, comingFrom);
        }
    }

    private boolean contains(NetworkNode node) {
        List<NetworkNode> nodes = this.getDestinationNodes();
        return nodes.contains(node);
    }

    //todo -- funker ikke
    public NetworkNode getPath(NetworkNode source, NetworkNode destination) {
        int index = this.getDestinationNodes().indexOf(destination);
        if (index < 0) {
            System.out.println("this destination does not exist");
            return null;
        }

        NetworkNode prevNode = this.getPreviousNodes().get(index);
        if (source.equals(prevNode)) return destination;
        return getPath(source, prevNode);
    }


    public void update(NetworkNode staringNode) {
        update(staringNode, new ArrayList<NetworkNode>(), new ArrayList<NetworkNode>(), 0);
    }

    private void update(NetworkNode staringNode, List<NetworkNode> visited, List<NetworkNode> nodesToVisit, int cost) {
        visited.add(staringNode);
        nodesToVisit.remove(staringNode);
        List<NetworkNode> neighbourList = staringNode.getNeighbours();

        NetworkNode bestNode = null;
        for (NetworkNode n : neighbourList) {
            this.updateTable(n, cost + n.getCost(), staringNode);

            boolean nodeShouldBeVisited = !visited.contains(n) && !nodesToVisit.contains(n);
            if (nodeShouldBeVisited) {
                nodesToVisit.add(n);
            }

            if (visited.contains(n)) continue;

            if (bestNode == null) {
                bestNode = n;
            } else if (bestNode.getCost() > n.getCost() && !visited.contains(n)) {
                bestNode = n;
            }
        }

        if (bestNode == null) return;

        update(bestNode, visited, nodesToVisit, cost + bestNode.getCost());
    }

    private List<NetworkNode> getDestinationNodes(){
        return this.table.get(0);
    }

    private List<Integer> getCosts(){
        return this.table.get(1);
    }

    private List<NetworkNode> getPreviousNodes(){
        return this.table.get(2);
    }


    @Override
    public String toString() {
        String returnString = "";
        for (int i = 0; i < table.get(0).size(); i++) {
            returnString += "Destination: " + table.get(0).get(i)
                    + " | Cost: " + table.get(1).get(i)
                    + " | Previous: " + table.get(2).get(i)
                    + "\n";
        }
        return returnString;
    }
}
