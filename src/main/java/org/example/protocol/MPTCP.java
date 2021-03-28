package org.example.protocol;

import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.address.Address;
import org.example.network.address.SimpleAddress;
import org.example.network.address.UUIDAddress;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;

import java.util.*;

public class MPTCP implements TCP{

    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    private final Queue<Packet> receivedPackets;
    private final List<Payload> payloadsToSend;
    private final TCP[] subflows;
    private final Address address;

    public MPTCP(int numberOfSubflows, int... receivingWindowCapacities) {
        if (receivingWindowCapacities.length != numberOfSubflows) throw new IllegalArgumentException("the number of receiving capacities does not match the given number of subflows");
        this.receivedPackets = new PriorityQueue<>(PACKET_COMPARATOR);
        this.payloadsToSend = new ArrayList<>();
        this.subflows = new TCP[numberOfSubflows];
        this.address = new UUIDAddress();
        for (int i = 0; i < numberOfSubflows; i++) {
            TCP tcp = new ClassicTCP.ClassicTCPBuilder()
                    .withReceivingWindowCapacity(receivingWindowCapacities[i])
                    .withAddress(new SimpleAddress("Subflow " + i + " " + this.address))
                    .withReceivedPacketsContainer(this.receivedPackets)
                    .withPayloadsToSend(this.payloadsToSend)
                    .build();
            this.subflows[i] = tcp;
        }
    }

    @Override
    public boolean outputBufferIsEmpty() {
        return this.payloadsToSend.isEmpty();
    }

    @Override
    public void updateRoutingTable() {
        for (TCP subflow : this.subflows) {
            subflow.updateRoutingTable();
        }
    }

    @Override
    public void route(Packet packet) {

    }

    @Override
    public long processingDelay() {
        return 0;
    }

    @Override
    public List<Channel> getChannels() {
        return null;
    }

    public Endpoint getEndpointToAddChannelTo(){
        for (Endpoint endpoint : this.subflows) {
            if (endpoint.getChannels().isEmpty()) return endpoint;
        }
        throw new IllegalStateException("no endpoints available for channel adding");
    }

    @Override
    public void addChannel(NetworkNode node) {
        for (TCP subflow : this.subflows){
            if (subflow.getChannels().isEmpty()){
                subflow.addChannel(node);
                return;
            }
        }
    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public Packet peekInputBuffer() {
        return null;
    }

    @Override
    public Packet dequeueInputBuffer() {
        return null;
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        return false;
    }

    @Override
    public boolean inputBufferIsEmpty() {
        return false;
    }

    @Override
    public int inputBufferSize() {
        return 0;
    }


    @Override
    public void run() {

    }

    public TCP[] getSubflows(){
        return this.subflows;
    }

    @Override
    public void connect(TCP host) {
        if (host instanceof MPTCP){
            MPTCP mptcpHost = (MPTCP) host;
            TCP[] hostSubflows = mptcpHost.getSubflows();
            int numberOfConnections = Math.min(this.subflows.length, hostSubflows.length);
            for (int i = 0; i < numberOfConnections; i++) {
                if (subflows[i].isConnected()) continue;
                this.subflows[i].connect(hostSubflows[i]);
            }
        }
    }

    @Override
    public void connect(Packet syn) {

    }

    @Override
    public void send(Payload payload) {
        payloadsToSend.add(payload);
    }

    @Override
    public Packet receive() {
        return this.receivedPackets.poll();
    }

    @Override
    public boolean isConnected() {
        for (TCP subflow : this.subflows) {
            if (!subflow.isConnected()) return false;
        }
        return true;
    }


    @Override
    public Channel getChannel() {
        return null;
    }

    @Override
    public List<Channel> getChannelsUsed() {
        List<Channel> usedChannels = new ArrayList<>(this.subflows.length);
        for (TCP subflow : this.subflows) {
            for (Channel channel : subflow.getChannelsUsed()) {
                usedChannels.add(channel);
            }
        }
        return usedChannels;
    }

    @Override
    public long getRTT() {
        return 0;
    }

    @Override
    public long getRTO() {
        return 0;
    }

    @Override
    public boolean handleIncoming() {
        System.out.println("incoming");
        for (TCP subflow : this.subflows) {
            subflow.handleIncoming();
        }
        return false;
    }

    @Override
    public List<Packet> trySend() {
        List<Packet> packetsSent = new ArrayList<>();
        for (TCP subflow : this.subflows) {
            for (Packet packet : subflow.trySend()) {
                packetsSent.add(packet);
            }
        }
        return packetsSent;
    }

    @Override
    public boolean canRetransmit(Packet packet) {
        return false;
    }

    @Override
    public Packet fastRetransmit() {
        return null;
    }

    @Override
    public boolean seriousLossDetected() {
        return false;
    }

    @Override
    public int getSendingWindowCapacity() {
        return 0;
    }
}
