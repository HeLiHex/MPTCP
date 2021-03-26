package org.example.protocol;

import org.example.data.Packet;
import org.example.data.Payload;
import org.example.network.Channel;
import org.example.network.address.Address;
import org.example.network.address.SimpleAddress;
import org.example.network.address.UUIDAddress;

import java.util.*;

public class MPTCP{

    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    private final Queue<Packet> receivedPackets;
    private final List<Payload> payloadsToSend;
    private TCP[] subflows;
    private Address address;
    private List<Channel> channelsUsed;



    public MPTCP(int numberOfSubflows, int... receivingWindowCapacities) {
        if (receivingWindowCapacities.length != numberOfSubflows) throw new IllegalArgumentException("the number of receiving capacities does not match the given number of subflows");
        this.receivedPackets = new PriorityQueue<>(PACKET_COMPARATOR);
        this.payloadsToSend = new ArrayList<>();
        this.subflows = new TCP[numberOfSubflows];
        this.address = new UUIDAddress();
        this.channelsUsed = new ArrayList<>();
        for (int i = 0; i < numberOfSubflows; i++) {
            TCP tcp = new ClassicTCP.ClassicTCPBuilder()
                    .withReceivingWindowCapacity(receivingWindowCapacities[i])
                    .withReceivedPacketsContainer(this.receivedPackets)
                    .withPayloadsToSend(this.payloadsToSend)
                    .build();
            this.subflows[i] = tcp;
        }
    }



}
