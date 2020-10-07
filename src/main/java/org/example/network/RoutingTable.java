package org.example.network;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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
        prevNodes.add(comingFrom);

        int prevIndex = nodes.indexOf(comingFrom);
        boolean nodeIsVisited = prevIndex >= 0;
        int newCost = nodeIsVisited ? cost + costs.get(prevIndex) : cost;

        costs.add(newCost);
    }

    private void updateEntry(NetworkNode nodeToUpdate, int cost, NetworkNode comingFrom) {
        int index = this.getDestinationNodes().indexOf(nodeToUpdate);
        List<Integer> costs = this.getCosts();
        List<NetworkNode> prevNodes = this.getPreviousNodes();

        int prevIndex = this.getDestinationNodes().indexOf(comingFrom);
        boolean nodeIsVisited = prevIndex >= 0;
        int newCost = nodeIsVisited ? cost + this.getCosts().get(prevIndex) : 0;

        if (costs.get(index) > newCost) {
            prevNodes.set(index, comingFrom);
            costs.set(index, cost + this.getDestinationNodes().get(prevIndex).getCost());

        }
    }

    private boolean contains(NetworkNode node) {
        List<NetworkNode> nodes = this.getDestinationNodes();
        return nodes.contains(node);
    }

    public NetworkNode getPath(NetworkNode source, NetworkNode destination) {
        if (destination == null){
            System.out.println("ERROR! The destination is null");
            return null;
        }

        int index = this.getDestinationNodes().indexOf(destination);
        if (index < 0) {
            System.out.println("Destination " + destination + " does not exist");
            return null;
        }

        NetworkNode prevNode = this.getPreviousNodes().get(index);
        if (source.equals(prevNode)) return destination;
        return getPath(source, prevNode);
    }


    public void update(NetworkNode staringNode) {
        djikstra(staringNode, new ArrayList<NetworkNode>(), new PriorityQueue<NetworkNode>());
    }

    private void djikstra(NetworkNode curNode, List<NetworkNode> visited, Queue<NetworkNode> priorityQueue){
        visited.add(curNode);
        List<NetworkNode> neighbouringNodes = curNode.getNeighbours();

        for (NetworkNode neighbour : neighbouringNodes) {
            priorityQueue.offer(neighbour);
            this.updateTable(neighbour, neighbour.getCost(), curNode);
        }

        while (!priorityQueue.isEmpty()){
            NetworkNode bestNode = priorityQueue.poll();
            if (visited.contains(bestNode)) continue;
            djikstra(bestNode, visited, priorityQueue);
        }
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
