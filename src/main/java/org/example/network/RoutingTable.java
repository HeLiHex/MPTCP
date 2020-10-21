package org.example.network;

import org.example.network.interfaces.NetworkNode;

import java.util.*;

public class RoutingTable {

    private Map<NetworkNode, Map.Entry<Channel, Integer>> table;


    public RoutingTable(NetworkNode node) {
        this.table = new HashMap<>();
    }



    private void updateTable(NetworkNode node, Channel channel) {
        if (this.table.containsKey(node)) {
            this.updateEntry(node, channel);
            return;
        }
        this.addEntry(node, channel);
    }

    private void addEntry(NetworkNode node, Channel channel) {
        if (channel == null){
            this.table.put(node, Map.entry(new Channel(node), 0));
            return;
        }

        boolean prevNodeInTable = table.containsKey(channel.getSource());
        int newCost = prevNodeInTable ? table.get(channel.getSource()).getValue() +  channel.getCost(): channel.getCost();

        table.put(node, Map.entry(channel, newCost));
    }

    private void updateEntry(NetworkNode node, Channel channel) {
        boolean prevNodeInTable = table.containsKey(channel.getSource());
        int newCost = prevNodeInTable ? table.get(channel.getSource()).getValue() + channel.getCost(): 0;
        if (newCost < this.table.get(node).getValue()){
            table.replace(node, Map.entry(channel, newCost));
        }
    }


    public void update(NetworkNode startingNode) {
        this.addEntry(startingNode, null);
        update(startingNode, new ArrayList<>(), new PriorityQueue<>());
    }

    private void update(NetworkNode curNode, List<NetworkNode> visited, Queue<Channel> priorityQueue){
        visited.add(curNode);

        for (Channel channel : curNode.getChannels()) {
            priorityQueue.offer(channel);
            this.updateTable(channel.getDestination(), channel);
        }

        while (!priorityQueue.isEmpty()){
            Channel bestChannel = priorityQueue.poll();
            if (visited.contains(bestChannel.getDestination())) continue;
            update(bestChannel.getDestination(), visited, priorityQueue);
        }
    }


    public Channel getPath(NetworkNode source, NetworkNode destination) {
        if (this.table.isEmpty()) throw new IllegalStateException("The routing table is empty");
        if (destination == null) throw new IllegalArgumentException("The destination can't be null");
        if (!this.table.containsKey(destination)) throw new IllegalArgumentException("Destination " + destination + " does not exist in this routing table");

        Channel curChannel = this.table.get(destination).getKey();
        NetworkNode prevNode = curChannel.getSource();
        if (source.equals(prevNode)) return curChannel;
        return getPath(source, prevNode);
    }

    @Override
    public String toString() {
        String result = "";
        for (NetworkNode name: this.table.keySet()){
            String node = name.toString();
            Map.Entry<Channel, Integer> value = this.table.get(name);
            String channel = value.getKey().toString();
            String cost = value.getValue().toString();
            result += "Node: " + node + "| Channel: " + channel + "| cost: " + cost +"\n";
        }
        return result;
    }


}
