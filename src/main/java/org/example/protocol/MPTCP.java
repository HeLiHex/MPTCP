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

    @Override
    public void addChannel(NetworkNode node) {

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
    public Channel getPath(NetworkNode destination) {
        return null;
    }

    @Override
    public void run() {

    }

    @Override
    public void connect(Endpoint host) {

    }

    @Override
    public void connect(Packet syn) {

    }

    @Override
    public void send(Payload payload) {

    }

    @Override
    public Packet receive() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Channel getChannel() {
        return null;
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
        return false;
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
