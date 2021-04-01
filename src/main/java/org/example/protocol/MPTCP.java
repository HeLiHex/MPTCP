package org.example.protocol;

import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.address.Address;
import org.example.network.address.SimpleAddress;
import org.example.network.address.UUIDAddress;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.protocol.window.receiving.SelectiveRepeat;
import org.javatuples.Pair;

import java.util.*;

public class MPTCP implements TCP{

    private static final Comparator<Packet> PACKET_INDEX_COMPARATOR = Comparator.comparingInt(Packet::getIndex);
    private final List<Packet> receivedPackets;
    private final List<Pair<Integer, Payload>> payloadsToSend;
    private final TCP[] subflows;
    private final Address address;
    private final ReceivingWindow receivingWindow;

    public MPTCP(int numberOfSubflows, int receivingWindowCapacity) {
        //if (receivingWindowCapacities.length != numberOfSubflows) throw new IllegalArgumentException("the number of receiving capacities does not match the given number of subflows");
        this.receivedPackets = new ArrayList<>();
        this.payloadsToSend = new ArrayList<>();
        this.subflows = new TCP[numberOfSubflows];
        this.address = new UUIDAddress();
        this.receivingWindow = new SelectiveRepeat(receivingWindowCapacity, PACKET_INDEX_COMPARATOR, this.receivedPackets);

        for (int i = 0; i < numberOfSubflows; i++) {
            TCP tcp = new ClassicTCP.ClassicTCPBuilder()
                    .withReceivingWindowCapacity(receivingWindowCapacity/(2+numberOfSubflows))
                    .withAddress(new SimpleAddress("Subflow " + i + " " + this.address))
                    .withReceivedPacketsContainer(this.receivedPackets)
                    .withPayloadsToSend(this.payloadsToSend)
                    .withReceivingWindow(this.receivingWindow)
                    .withMainFlow(this)
                    .build();
            this.subflows[i] = tcp;
        }
    }

    @Override
    public TCP getMainFlow() {
        return this;
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
        throw new IllegalStateException("deprecated");
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
        throw new IllegalStateException("deprecated");
    }

    @Override
    public Packet peekInputBuffer() {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public Packet dequeueInputBuffer() {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public boolean inputBufferIsEmpty() {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public int inputBufferSize() {
        throw new IllegalStateException("deprecated");
    }


    @Override
    public void run() {
        throw new IllegalStateException("deprecated");
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
        throw new IllegalStateException("deprecated");
    }

    @Override
    public void send(Payload payload) {
        if (this.payloadsToSend.isEmpty()) {
            this.payloadsToSend.add(Pair.with(0, payload));
            return;
        }
        int indexOfLastAdded = this.payloadsToSend.get(this.payloadsToSend.size() - 1).getValue0();
        this.payloadsToSend.add(Pair.with(indexOfLastAdded + 1, payload));
    }

    @Override
    public Packet receive() {
        System.out.println(this.receivingWindow);
        if (this.receivedPackets.isEmpty()) return null;
        return this.receivedPackets.remove(0);
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
        throw new IllegalStateException("deprecated");
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
        throw new IllegalStateException("deprecated");
    }

    @Override
    public long getRTO() {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public long afterConnectSendDelay() {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public boolean handleIncoming() {
        System.out.println("incoming");
        for (TCP subflow : this.subflows) {
            try{
                subflow.handleIncoming();
            }catch (IllegalArgumentException e){
                continue;
            }
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
        for (TCP subflow : this.subflows) {
            if (subflow.canRetransmit(packet)) return true;
        }
        return false;
    }

    @Override
    public Packet fastRetransmit() {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public boolean seriousLossDetected() {
        throw new IllegalStateException("deprecated");
    }

    @Override
    public int getSendingWindowCapacity() {
        throw new IllegalStateException("deprecated");
    }
}
