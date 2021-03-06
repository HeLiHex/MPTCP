package org.example.protocol;

import org.example.data.*;
import org.example.network.Channel;
import org.example.network.Routable;
import org.example.network.address.Address;
import org.example.network.address.UUIDAddress;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.protocol.window.receiving.SelectiveRepeat;
import org.example.protocol.window.sending.SendingWindow;
import org.example.protocol.window.sending.SlidingWindow;
import org.example.simulator.statistics.TCPStats;
import org.example.util.Util;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassicTCP extends Routable implements TCP {

    private static final Comparator<Packet> SENDING_WINDOW_PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    private final TCPStats tcpStats;
    private final Logger logger = Logger.getLogger(ClassicTCP.class.getSimpleName());
    private final List<Packet> receivedPackets;
    private final List<Pair<Integer, Payload>> payloadsToSend;
    private final int thisReceivingWindowCapacity;
    private final boolean isReno;
    private final TCP mainFlow;
    private int otherReceivingWindowCapacity;
    private int initialSequenceNumber;
    private long rtt;
    private long rttStartTimer = 0;
    private boolean timerStarted = false;
    private boolean rttSet = false;
    private SendingWindow sendingWindow;

    private ClassicTCP(int thisReceivingWindowCapacity,
                       List<Packet> receivedPackets,
                       List<Pair<Integer, Payload>> payloadsToSend,
                       boolean isReno,
                       Address address,
                       TCP mainFlow,
                       ReceivingWindow receivingWindow) {
        super(receivingWindow, address);
        this.receivedPackets = receivedPackets;
        this.payloadsToSend = payloadsToSend;
        this.thisReceivingWindowCapacity = thisReceivingWindowCapacity;
        this.isReno = isReno;

        if (mainFlow == null) {
            this.mainFlow = this;
        } else {
            this.mainFlow = mainFlow;
        }

        this.tcpStats = new TCPStats(address);
    }


    @Override
    public void connect(TCP host) {
        if (this.isConnected()) return;
        this.initialSequenceNumber = Util.getNextRandomInt(10000);
        Packet syn = new PacketBuilder()
                .withDestination(host)
                .withOrigin(this)
                .withFlags(Flag.SYN)
                .withSequenceNumber(this.initialSequenceNumber)
                .withPayload(new Message(this.thisReceivingWindowCapacity + ""))
                .build();
        this.route(syn);
        if (!this.timerStarted) {
            this.rttStartTimer = Util.seeTime();
            this.timerStarted = true;
        }
    }

    public void continueConnect(Packet synAck) {
        Endpoint host = synAck.getOrigin();
        if (synAck.hasAllFlags(Flag.SYN, Flag.ACK)) {
            if (synAck.getAcknowledgmentNumber() != this.initialSequenceNumber + 1) {
                return;
            }
            this.otherReceivingWindowCapacity = Integer.parseInt(synAck.getPayload().toString());
            int finalSeqNum = synAck.getAcknowledgmentNumber();
            int ackNum = synAck.getSequenceNumber() + 1;
            Packet ack = new PacketBuilder()
                    .withDestination(host)
                    .withOrigin(this)
                    .withFlags(Flag.ACK)
                    .withSequenceNumber(finalSeqNum)
                    .withAcknowledgmentNumber(ackNum)
                    .build();
            this.route(ack);

            this.setRTT();
            this.setConnection(new Connection(this, host, finalSeqNum, ackNum));

            this.logger.log(Level.INFO, () -> "connection established with host: " + this.getConnection());
        }

    }

    @Override
    public void connect(Packet syn) {
        if (this.isConnected()) return;
        Endpoint node = syn.getOrigin();
        var seqNum = Util.getNextRandomInt(10000);
        var ackNum = syn.getSequenceNumber() + 1;
        this.otherReceivingWindowCapacity = Integer.parseInt(syn.getPayload().toString());
        Packet synAck = new PacketBuilder()
                .withDestination(node)
                .withOrigin(this)
                .withFlags(Flag.SYN, Flag.ACK)
                .withSequenceNumber(seqNum)
                .withAcknowledgmentNumber(ackNum)
                .withPayload(new Message(this.thisReceivingWindowCapacity + ""))
                .build();
        this.route(synAck);

        if (!this.timerStarted) {
            this.rttStartTimer = Util.seeTime();
            this.timerStarted = true;
        }

        this.setConnection(new Connection(this, node, seqNum, ackNum));

        this.logger.log(Level.INFO, () -> "connection established with client: " + this.getConnection());

    }

    private void setRTT() {
        if (rttSet) return;
        this.rtt = Util.seeTime() - this.rttStartTimer;
        this.rttSet = true;
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
        if (this.receivedPackets.isEmpty()) return null;
        return this.receivedPackets.remove(0);
    }

    @Override
    public Channel getChannel() {
        return this.getChannels().get(0);
    }

    public int getThisReceivingWindowCapacity() {
        return this.thisReceivingWindowCapacity;
    }

    public int getOtherReceivingWindowCapacity() {
        return this.otherReceivingWindowCapacity;
    }

    @Override
    public boolean isConnected() {
        try {
            return this.getSendingWindow().getConnection() != null;
        } catch (IllegalAccessException e) {
            //no SendingWindow means no connection
            return false;
        }
    }

    public Connection getConnection() {
        try {
            return this.getSendingWindow().getConnection();
        } catch (IllegalAccessException e) {
            //No SendingWindow means no connection is established
            return null;
        }
    }

    private void setConnection(Connection connection) {
        this.sendingWindow = new SlidingWindow(this.otherReceivingWindowCapacity, this.isReno, connection, SENDING_WINDOW_PACKET_COMPARATOR, this.payloadsToSend, this.tcpStats);
    }

    @Override
    public long getRTO() {
        return 3 * this.rtt;
    }


    @Override
    public Packet fastRetransmit() {
        try {
            return this.getSendingWindow().fastRetransmit();
        } catch (IllegalAccessException e) {
            //no Sending Window results in no retransmit
            return null;
        }
    }

    @Override
    public long delay() {
        return 10;
    }

    @Override
    public TCP getMainFlow() {
        return this.mainFlow;
    }

    @Override
    public int getNumberOfFlows() {
        return 1;
    }

    public SendingWindow getSendingWindow() throws IllegalAccessException {
        if (this.sendingWindow != null) return this.sendingWindow;
        throw new IllegalAccessException("SendingWindow is null");
    }

    public ReceivingWindow getReceivingWindow() throws IllegalAccessException {
        if (this.inputBuffer instanceof ReceivingWindow) return (ReceivingWindow) this.inputBuffer;
        throw new IllegalAccessException("The outputBuffer is not of type ReceivingWindow");
    }

    private boolean packetIsFromValidConnection(Packet packet) {
        if (packet.hasAllFlags(Flag.SYN) && !this.isConnected()) return true;
        if (!this.isConnected()) return false;

        var conn = this.getConnection();
        if (packet.hasAllFlags(Flag.ACK)) return true;


        try {
            boolean inWindow = this.getReceivingWindow().inReceivingWindow(packet, conn);
            if (!inWindow) {
                this.ack(this.getReceivingWindow().ackThis(this.getSendingWindow().getConnection().getConnectedNode()));
                return false;
            }

            return packet.getOrigin().equals(conn.getConnectedNode())
                    && packet.getDestination().equals(conn.getConnectionSource());
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    @Override
    public boolean outputBufferIsEmpty() {
        return this.sendingWindow.isEmpty();
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        if (packetIsFromValidConnection(packet)) {
            if (super.enqueueInputBuffer(packet)) {
                if (!packet.hasAllFlags(Flag.ACK) && !packet.hasAllFlags(Flag.SYN)) this.tcpStats.packetArrival(packet);
                return true;
            }
            return true;
        }
        // return true because the packet has arrived the endpoint
        // the packet is not added to the input buffer, but it is checked
        return false;
    }

    private boolean unconnectedInputHandler() {
        if (this.inputBuffer.isEmpty()) return false;
        if (!this.peekInputBuffer().getDestination().equals(this)) return false;

        var packet = this.dequeueInputBuffer();

        if (packet.hasAllFlags(Flag.SYN, Flag.ACK)) {
            this.continueConnect(packet);
            return true;
        }

        if (packet.hasAllFlags(Flag.SYN)) {
            this.connect(packet);
            return true;
        }
        return false;
    }


    public boolean canRetransmit(Packet packet) {
        try {
            return this.getSendingWindow().canRetransmit(packet);
        } catch (IllegalAccessException e) {
            //should not retransmit without SendingWindow
            return false;
        }
    }


    private void ack(Packet packet) {
        assert packet != null : "Packet is null";

        if (packet.getSequenceNumber() == -1) return;

        if (packet.getOrigin() == null) {
            //can't call ack on packet with no origin
            Endpoint connectedNode;
            try {
                connectedNode = this.getSendingWindow().getConnection().getConnectedNode();
            } catch (IllegalAccessException e) {
                //should not be able to ack without a SendingWindow
                return;
            }
            packet = new PacketBuilder()
                    .withDestination(connectedNode)
                    .withOrigin(this)
                    .withSequenceNumber(packet.getSequenceNumber())
                    .withAcknowledgmentNumber(packet.getAcknowledgmentNumber())
                    .withPayload(packet.getPayload())
                    .build();

        }
        Packet ack = new PacketBuilder().ackBuild(packet);
        this.route(ack);
    }


    public boolean handleIncoming() {
        if (!isConnected()) return unconnectedInputHandler();

        try {
            var receivingWindow = this.getReceivingWindow();
            var packetReceived = receivingWindow.receive(this.getSendingWindow());
            if (packetReceived && receivingWindow.shouldAck()) {
                try {
                    this.ack(receivingWindow.ackThis(this.getSendingWindow().getConnection().getConnectedNode()));
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }

            this.setRTT();

            var packetToFastRetransmit = this.fastRetransmit();
            if (packetToFastRetransmit != null) {
                this.route(packetToFastRetransmit);
                this.tcpStats.packetFastRetransmit();
            }
            return true;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("TCP is connected but no ReceivingWindow or SendingWindow is established");
        }
    }


    @Override
    public List<Packet> trySend() {
        return this.trySend(new ArrayList<>());
    }

    private List<Packet> trySend(List<Packet> packetsSent) {
        if (this.sendingWindow == null) return packetsSent;
        if (this.sendingWindow.isQueueEmpty()) return packetsSent;
        if (this.sendingWindow.isWaitingForAck()) return packetsSent;

        var packetToSend = this.sendingWindow.send();
        assert packetToSend != null;

        this.route(packetToSend);
        this.tcpStats.packetSend();

        packetsSent.add(packetToSend);
        return trySend(packetsSent);
    }

    @Override
    public TCPStats getStats() {
        return tcpStats;
    }

    @Override
    public void run() {
        this.handleIncoming();
        this.trySend();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    public static class ClassicTCPBuilder {

        private int receivingWindowCapacity = 7;
        private List<Packet> receivedPacketsContainer = new ArrayList<>();
        private List<Pair<Integer, Payload>> payloadsToSend = new ArrayList<>();
        private boolean isReno = true;
        private Address address = new UUIDAddress();
        private TCP mainflow = null;
        private ReceivingWindow receivingWindow = new SelectiveRepeat(this.receivingWindowCapacity, Comparator.comparingInt(Packet::getSequenceNumber), this.receivedPacketsContainer);

        public ClassicTCPBuilder withReceivingWindowCapacity(int receivingWindowCapacity) {
            this.receivingWindowCapacity = receivingWindowCapacity;
            return this;
        }

        public ClassicTCPBuilder withReceivedPacketsContainer(List<Packet> receivedPacketsContainer) {
            this.receivedPacketsContainer = receivedPacketsContainer;
            return this;
        }

        public ClassicTCPBuilder withPayloadsToSend(List<Pair<Integer, Payload>> payloadsToSend) {
            this.payloadsToSend = payloadsToSend;
            return this;
        }

        public ClassicTCPBuilder setTahoe() {
            this.isReno = false;
            return this;
        }

        public ClassicTCPBuilder setReno() {
            this.isReno = true;
            return this;
        }

        public ClassicTCPBuilder withAddress(Address address) {
            this.address = address;
            return this;
        }

        public ClassicTCPBuilder withMainFlow(TCP tcp) {
            this.mainflow = tcp;
            return this;
        }

        public ClassicTCPBuilder withReceivingWindow(ReceivingWindow receivingWindow) {
            this.receivingWindow = receivingWindow;
            return this;
        }

        public ClassicTCP build() {
            return new ClassicTCP(this.receivingWindowCapacity,
                    this.receivedPacketsContainer,
                    this.payloadsToSend,
                    this.isReno,
                    this.address,
                    this.mainflow,
                    this.receivingWindow);
        }
    }

}
