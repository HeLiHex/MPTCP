package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.example.data.Payload;
import org.example.protocol.Connection;
import org.example.protocol.window.Window;
import org.example.simulator.Statistics;
import org.example.util.BoundedQueue;

import java.util.Comparator;
import java.util.List;

public class SlidingWindow extends Window implements SendingWindow, BoundedQueue<Packet> {

    private static final int DEFAULT_CONGESTION_WINDOW_CAPACITY = 1;
    private final List<Payload> payloadsToSend;
    private final int receiverWindowSize;
    private final boolean isReno;
    private boolean seriousLossDetected = false;
    private int numPacketsReceivedWithoutIncreasingWindow;
    private int ssthresh;
    private int dupAckCount;
    private boolean fastRetransmitted;
    private final Connection connection;

    public SlidingWindow(int receiverWindowCapacity, boolean isReno, Connection connection, Comparator<Packet> comparator, List<Payload> payloadsToSend) {
        super(DEFAULT_CONGESTION_WINDOW_CAPACITY, comparator);
        this.payloadsToSend = payloadsToSend;
        this.receiverWindowSize = receiverWindowCapacity;
        this.numPacketsReceivedWithoutIncreasingWindow = 0;
        this.ssthresh = receiverWindowCapacity; //initial value is essentially no ssthresh
        this.isReno = isReno;
        this.dupAckCount = 0;
        this.fastRetransmitted = false;
        this.connection = connection;

    }

    @Override
    public boolean isWaitingForAck() {
        return this.isFull();
    }

    @Override
    public void ackReceived(Packet ack) {
        int ackIndex = this.sendingPacketIndex(ack);

        boolean dupAck = ackIndex == -1;
        if (dupAck) {
            this.dupAckCount++;
        }

        boolean isValidAckIndex = true;//ackIndex < this.getWindowCapacity();
        System.out.println("valid ack: " + isValidAckIndex);
        if (isValidAckIndex) {
            for (int i = 0; i <= ackIndex; i++) {
                Packet packetPolled = this.poll();
                System.out.println("packet acked: " + packetPolled);
                this.increase();
                this.seriousLossDetected = false;
            }
        }
        this.connection.update(ack);
    }

    @Override
    public Packet send() {
        int nextPacketSeqNum = this.connection.getNextSequenceNumber() + this.size();
        Packet packet = new PacketBuilder()
                .withConnection(this.connection)
                .withPayload(this.payloadsToSend.remove(0))
                .withSequenceNumber(nextPacketSeqNum)
                .build();

        if (this.contains(packet)) throw new IllegalStateException("can't add same packet twice");
        if (super.offer(packet)) {
            return packet;
        }
        return null;
    }


    @Override
    public boolean canRetransmit(Packet packet) {
        if (this.contains(packet)) {
            if (this.sendingPacketIndex(packet) == 0) {
                if (this.fastRetransmitted) this.fastRetransmitted = false;
                else this.decrease();
            }
            if (this.sendingPacketIndex(packet) >= this.getWindowCapacity() - 1) this.seriousLossDetected = true;
            this.dupAckCount = 0;
            return true;
        }
        return false;
    }

    @Override
    public Packet fastRetransmit() {
        if (this.dupAckCount >= 3) {
            this.dupAckCount = 0;
            this.decrease(false);
            this.fastRetransmitted = true;
            return this.peek();
        }
        return null;
    }

    @Override
    public void increase() {
        if (this.getWindowCapacity() >= this.receiverWindowSize) return;
        if (this.ssthresh > this.getWindowCapacity()) slowStart();
        else congestionAvoidance();
    }

    private void slowStart() {
        this.setBound(this.getWindowCapacity() + 1);
        Statistics.trackCwnd(this.getWindowCapacity());
    }

    private void congestionAvoidance() {
        boolean allPacketsInOneWindowReceived =
                this.numPacketsReceivedWithoutIncreasingWindow % this.getWindowCapacity() == 0
                        && this.numPacketsReceivedWithoutIncreasingWindow != 0;
        if (allPacketsInOneWindowReceived) {
            this.numPacketsReceivedWithoutIncreasingWindow = 0;
            this.setBound(this.getWindowCapacity() + 1);
            Statistics.trackCwnd(this.getWindowCapacity());
            return;
        }
        this.numPacketsReceivedWithoutIncreasingWindow++;
    }

    @Override
    public void decrease() {
        this.decrease(true);
    }

    public void decrease(boolean timeout) {
        Statistics.trackCwnd(this.getWindowCapacity());

        this.ssthresh = this.findNewSSThresh();
        int newWindowSize = this.findNewWindowSize(timeout);
        this.setBound(newWindowSize);

        Statistics.trackCwnd(this.getWindowCapacity());
    }


    private int findNewWindowSize(boolean timeout) {
        if (this.isReno && !timeout) return this.ssthresh;
        return DEFAULT_CONGESTION_WINDOW_CAPACITY;
    }

    private int findNewSSThresh() {
        return Math.max((int) (this.getWindowCapacity() / 2.0), DEFAULT_CONGESTION_WINDOW_CAPACITY);
    }

    @Override
    public boolean isSeriousLossDetected() {
        return this.seriousLossDetected;
    }

    @Override
    public int packetsInTransmission() {
        return this.size();
    }

    @Override
    public boolean inSendingWindow(Packet packet) {
        int packetIndex = sendingPacketIndex(packet);
        return inWindow(packetIndex);
    }

    @Override
    public int sendingPacketIndex(Packet packet) {
        int packetSeqNum = packet.getSequenceNumber();
        int connSeqNum = this.connection.getNextSequenceNumber();
        return packetSeqNum - connSeqNum;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean offer(Packet packet) {
        return this.payloadsToSend.add(packet.getPayload());
    }

    @Override
    public int queueSize() {
        return this.payloadsToSend.size();
    }

    @Override
    public boolean isQueueEmpty() {
        return this.payloadsToSend.isEmpty();
    }


}
