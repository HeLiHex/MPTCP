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
import org.example.util.Util;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassicTCP extends Routable implements TCP {

    private static final double NOISE_TOLERANCE = 100.0;
    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);
    private final Logger logger = Logger.getLogger(ClassicTCP.class.getSimpleName());
    private final List<Packet> receivedPackets;
    private final List<Payload> payloadsToSend;
    private final int thisReceivingWindowCapacity;
    private final boolean isReno;

    private int otherReceivingWindowCapacity;
    private int initialSequenceNumber;
    private long rtt;

    private long afterConnectSendDelay = 100000;

    private SendingWindow sendingWindow;

    private final TCP mainFlow;

    private ClassicTCP(int thisReceivingWindowCapacity, List<Packet> receivedPackets, List<Payload> payloadsToSend, boolean isReno, Address address, TCP mainFlow) {
        super(new SelectiveRepeat(thisReceivingWindowCapacity, PACKET_COMPARATOR, receivedPackets),
                NOISE_TOLERANCE,
                address
        );
        this.receivedPackets = receivedPackets;
        this.payloadsToSend = payloadsToSend;
        this.thisReceivingWindowCapacity = thisReceivingWindowCapacity;
        this.isReno = isReno;

        if (mainFlow == null){
            this.mainFlow = this;
        }else{
            this.mainFlow = mainFlow;
        }
    }


    @Override
    public void connect(TCP host) {
        this.initialSequenceNumber = Util.getNextRandomInt(100);
        Packet syn = new PacketBuilder()
                .withDestination(host)
                .withOrigin(this)
                .withFlags(Flag.SYN)
                .withSequenceNumber(this.initialSequenceNumber)
                .withPayload(new Message(this.thisReceivingWindowCapacity + ""))
                .build();
        this.route(syn);
    }

    public void continueConnect(Packet synAck) {
        Endpoint host = synAck.getOrigin();
        if (synAck.hasAllFlags(Flag.SYN, Flag.ACK)) {
            if (synAck.getAcknowledgmentNumber() != this.initialSequenceNumber + 1) {
                this.logger.log(Level.INFO, "Wrong ack number");
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

            this.rtt = Util.seeTime();
            this.setConnection(new Connection(this, host, finalSeqNum, ackNum));

            this.logger.log(Level.INFO, () -> "connection established with host: " + this.getConnection());
        }

    }

    @Override
    public void connect(Packet syn) {
        Endpoint node = syn.getOrigin();
        int seqNum = Util.getNextRandomInt(100);
        int ackNum = syn.getSequenceNumber() + 1;
        this.otherReceivingWindowCapacity = Integer.parseInt(syn.getPayload().toString());
        Packet synAck = new PacketBuilder()
                .withDestination(node)
                .withOrigin(this)
                .withFlags(Flag.SYN, Flag.ACK)
                .withSequenceNumber(seqNum)
                .withAcknowledgmentNumber(ackNum)
                .withPayload(new Message(this.thisReceivingWindowCapacity + ""))
                .build();
        this.rtt = Util.seeTime() * 2;
        this.route(synAck);

        this.setConnection(new Connection(this, node, seqNum, ackNum));

        this.logger.log(Level.INFO, () -> "connection established with client: " + this.getConnection());

    }

    @Override
    public void send(Payload payload) {
        this.payloadsToSend.add(payload);
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
        this.sendingWindow = new SlidingWindow(this.otherReceivingWindowCapacity, this.isReno, connection, PACKET_COMPARATOR, this.payloadsToSend);
    }

    @Override
    public long getRTT() {
        return this.rtt;
    }

    @Override
    public long getRTO() {
        return 3 * this.rtt;
    }

    @Override
    public long afterConnectSendDelay() {
        long delay = this.afterConnectSendDelay;
        this.afterConnectSendDelay = 0;
        return delay;
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
    public long processingDelay() {
        return super.processingDelay() * 2;
    }

    @Override
    public int getSendingWindowCapacity() {
        try {
            return this.getSendingWindow().getWindowCapacity();
        } catch (IllegalAccessException e) {
            //todo - is one correct?
            return 0;
        }
    }

    @Override
    public TCP getMainFlow() {
        return this.mainFlow;
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
        if (packet.hasAllFlags(Flag.SYN)) return true;
        if (!this.isConnected()) return false;

        Connection conn = this.getConnection();
        if (packet.hasAllFlags(Flag.ACK)) return true;

        try {
            return packet.getOrigin().equals(conn.getConnectedNode())
                    && packet.getDestination().equals(conn.getConnectionSource())
                    && this.getReceivingWindow().inReceivingWindow(packet, conn);
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
            return super.enqueueInputBuffer(packet);
        }
        this.logger.log(Level.INFO, "packet was not added due to non valid connection");
        return false;
    }

    private boolean unconnectedInputHandler() {
        if (this.inputBuffer.isEmpty()) return false;
        Packet packet = this.dequeueInputBuffer();

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

    public boolean seriousLossDetected() {
        try {
            return this.getSendingWindow().isSeriousLossDetected();
        } catch (IllegalAccessException e) {
            //can't detect loss without SendingWindow
            return false;
        }
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

        if (packet.getOrigin() == null){
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
            ReceivingWindow receivingWindow = this.getReceivingWindow();
            boolean packetReceived = receivingWindow.receive(this.getSendingWindow());
            if (packetReceived && receivingWindow.shouldAck()) {
                this.ack(receivingWindow.ackThis());
                return true;
            }
            //ACK is received and nothing more should be done
            return false;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("TCP is connected but no ReceivingWindow or SendingWindow is established");
        }
    }
    @Override
    public List<Packet> trySend() {
        if (this.sendingWindow == null) return new ArrayList<>(0);
        if (this.sendingWindow.isQueueEmpty()) return new ArrayList<>(0);
        if (this.sendingWindow.isWaitingForAck()) return new ArrayList<>(0);

        List<Packet> packetsSent = new ArrayList<>(1);

        Packet packetToSend = this.sendingWindow.send();
        assert packetToSend != null;

        this.route(packetToSend);
        packetsSent.add(packetToSend);
        return packetsSent;
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
        private List<Payload> payloadsToSend = new ArrayList<>();
        private boolean isReno = true;
        private Address address = new UUIDAddress();
        private TCP mainflow = null;

        public ClassicTCPBuilder withReceivingWindowCapacity(int receivingWindowCapacity) {
            this.receivingWindowCapacity = receivingWindowCapacity;
            return this;
        }

        public ClassicTCPBuilder withReceivedPacketsContainer(List<Packet> receivedPacketsContainer) {
            this.receivedPacketsContainer = receivedPacketsContainer;
            return this;
        }

        public ClassicTCPBuilder withPayloadsToSend(List<Payload> payloadsToSend) {
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

        public ClassicTCPBuilder withAddress(Address address){
            this.address = address;
            return this;
        }

        public ClassicTCPBuilder withMainFlow(TCP tcp){
            this.mainflow = tcp;
            return this;
        }

        public ClassicTCP build() {
            return new ClassicTCP(this.receivingWindowCapacity, this.receivedPacketsContainer, this.payloadsToSend, this.isReno, this.address, this.mainflow);
        }
    }

}
