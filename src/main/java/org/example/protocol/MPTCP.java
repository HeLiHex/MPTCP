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
import org.example.simulator.statistics.TCPStats;
import org.javatuples.Pair;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MPTCP implements TCP{

    private static final String DEPRECATED_STRING = "DEPRECATED";
    private static final Comparator<Packet> PACKET_INDEX_COMPARATOR = Comparator.comparingInt(Packet::getIndex);
    private final List<Packet> receivedPackets;
    private final List<Pair<Integer, Payload>> payloadsToSend;
    private final TCP[] subflows;
    private final Address address;
    private final ReceivingWindow receivingWindow;

    public MPTCP(int numberOfSubflows, int receivingWindowCapacity) {
        this.receivedPackets = new ArrayList<>();
        this.payloadsToSend = new ArrayList<>();
        this.subflows = new TCP[numberOfSubflows];
        this.address = new UUIDAddress();
        this.receivingWindow = new SelectiveRepeat(receivingWindowCapacity, PACKET_INDEX_COMPARATOR, this.receivedPackets);

        for (var i = 0; i < numberOfSubflows; i++) {
            TCP tcp = new ClassicTCP.ClassicTCPBuilder()
                    .withReceivingWindowCapacity(receivingWindowCapacity/numberOfSubflows)
                    .withAddress(new SimpleAddress("Subflow " + i + " " + this.address))
                    .withReceivedPacketsContainer(this.receivedPackets)
                    .withPayloadsToSend(this.payloadsToSend)
                    .withReceivingWindow(this.receivingWindow)
                    .withMainFlow(this)
                    .setReno()
                    .build();
            this.subflows[i] = tcp;
        }
    }

    @Override
    public TCP getMainFlow() {
        return this;
    }

    @Override
    public int getNumberOfFlows() {
        return this.subflows.length;
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
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public long processingDelay() {
        return 1000;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channels = new ArrayList<>();
        for (TCP subflow : this.subflows) {
            for (Channel c  : subflow.getChannels()) {
                channels.add(c);
            }
        }
        return channels;
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
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public Packet peekInputBuffer() {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public Packet dequeueInputBuffer() {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public boolean inputBufferIsEmpty() {
        return this.receivingWindow.isEmpty();
    }

    @Override
    public int inputBufferSize() {
        return this.receivingWindow.size();
    }


    @Override
    public void run() {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    public TCP[] getSubflows(){
        return this.subflows;
    }

    @Override
    public void connect(TCP host) {
        if (!(host instanceof MPTCP)) {
            this.subflows[0].connect(host);
            return;
        }
        var mptcpHost = (MPTCP) host;
        TCP[] hostSubflows = mptcpHost.getSubflows();

        for (var i = 0; i < this.subflows.length; i++) {
            TCP cFlow = this.subflows[i];
            if (cFlow.isConnected()) continue;
            for (var j = i; j < hostSubflows.length; j++) {
                TCP hFlow = hostSubflows[i];
                if (hFlow.isConnected()) continue;
                try {
                    cFlow.connect(hFlow);
                    break;
                }catch (IllegalArgumentException e){
                    Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "connection failed");
                }
            }
        }
    }

    @Override
    public void connect(Packet syn) {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public void send(Payload payload) {
        // avoiding duplicate code
        // adds to the payloadsToSend list that is shared among all subflows
        // therefore, it does not matter which subflow does this
        this.subflows[0].send(payload);
    }

    @Override
    public Packet receive() {
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
        throw new IllegalStateException(DEPRECATED_STRING);
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
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public long getRTO() {
        return this.subflows[0].getRTO();
        //throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public long afterConnectSendDelay() {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public boolean handleIncoming() {
        for (var i = 0; i < this.subflows.length; i++) {
            for (TCP subflow : this.subflows) {
                try{
                    subflow.handleIncoming();
                }catch (IllegalArgumentException e){
                    //do nothing, continue loop
                }
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

    public TCPStats[] getTcpStats() {
        var tcpStats = new TCPStats[this.subflows.length];
        for (var i = 0; i < this.subflows.length; i++) {
            tcpStats[i] = ((ClassicTCP)this.subflows[i]).getStats();
        }
        return tcpStats;
    }

    @Override
    public Packet fastRetransmit() {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public boolean seriousLossDetected() {
        throw new IllegalStateException(DEPRECATED_STRING);
    }

    @Override
    public int getSendingWindowCapacity() {
        throw new IllegalStateException(DEPRECATED_STRING);
    }
}
