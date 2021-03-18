package org.example.protocol;

import org.example.data.*;
import org.example.network.Channel;
import org.example.network.RoutableEndpoint;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.window.receiving.ReceivingWindow;
import org.example.protocol.window.receiving.SelectiveRepeat;
import org.example.protocol.window.sending.SendingWindow;
import org.example.protocol.window.sending.SlidingWindow;
import org.example.util.BoundedPriorityBlockingQueue;
import org.example.util.Util;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassicTCP extends RoutableEndpoint implements TCP {

    private final Logger logger = Logger.getLogger(ClassicTCP.class.getSimpleName());

    private static final int BUFFER_SIZE = 10000;
    private static final double NOISE_TOLERANCE = 100.0;
    private static final Comparator<Packet> PACKET_COMPARATOR = Comparator.comparingInt(Packet::getSequenceNumber);

    private final Queue<Packet> receivedPackets;
    private final List<Payload> payloadsToSend;

    private final int thisReceivingWindowCapacity;
    private int otherReceivingWindowCapacity;
    private Connection connection;
    private int initialSequenceNumber;
    private long rtt;


    public ClassicTCP(int thisReceivingWindowCapacity) {
        super(new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                new BoundedPriorityBlockingQueue<>(BUFFER_SIZE, PACKET_COMPARATOR),
                NOISE_TOLERANCE
        );
        this.receivedPackets = new PriorityQueue<>(PACKET_COMPARATOR);
        this.payloadsToSend = new ArrayList<>();
        this.thisReceivingWindowCapacity = thisReceivingWindowCapacity;
    }

    @Override
    public void connect(Endpoint host) {
        this.connection = null;
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
            this.createWindows();

            //this.logger.log(Level.INFO, () -> "connection established with host: " + this.getConnection());
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
        this.createWindows();

    }

    @Override
    public void send(Payload payload) {
        this.payloadsToSend.add(payload);
    }

    @Override
    public Packet receive() {
        return this.receivedPackets.poll();
    }

    @Override
    public void close() {
        Packet fin = new PacketBuilder()
                .withConnection(this.getConnection())
                .withFlags(Flag.FIN)
                .build();
        this.route(fin);
    }

    @Override
    public Channel getChannel() {
        if (this.isConnected()) {
            return this.getPath(this.getConnection().getConnectedNode());
        }
        //todo - what happens with MPTCP
        return this.getChannel(0);
    }

    private Channel getChannel(int channelIndex) {
        return this.getChannels().get(channelIndex);
    }

    public int getThisReceivingWindowCapacity() {
        return this.thisReceivingWindowCapacity;
    }

    public int getOtherReceivingWindowCapacity() {
        return this.otherReceivingWindowCapacity;
    }

    @Override
    public boolean isConnected() {
        return this.connection != null;
    }

    public Connection getConnection() {
        return this.connection;
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
    public Packet fastRetransmit() {
        try {
            return this.getSendingWindow().fastRetransmit();
        } catch (IllegalAccessException e) {
            //should not fast retransmit if there is no sending window
            return null;
        }
    }

    @Override
    public long processingDelay() {
        return super.processingDelay()*2;
    }

    @Override
    public int getSendingWindowCapacity() {
        try {
            return this.getSendingWindow().getWindowCapacity();
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private void setConnection(Connection connection) {
        this.connection = connection;
    }

    private void createWindows(){
        this.outputBuffer = new SlidingWindow(this.otherReceivingWindowCapacity, true, this.connection, PACKET_COMPARATOR, this.payloadsToSend);
        this.inputBuffer = new SelectiveRepeat(this.thisReceivingWindowCapacity, this.connection, PACKET_COMPARATOR, this.receivedPackets);
    }

    public SendingWindow getSendingWindow() throws IllegalAccessException {
        if (this.outputBuffer instanceof SendingWindow) return (SendingWindow) this.outputBuffer;
        throw new IllegalAccessException("The outputBuffer is not of type SendingWindow");
    }

    public ReceivingWindow getReceivingWindow() throws IllegalAccessException {
        if (this.inputBuffer instanceof ReceivingWindow) return (ReceivingWindow) this.inputBuffer;
        throw new IllegalAccessException("The outputBuffer is not of type ReceivingWindow");
    }

    private boolean packetIsFromValidConnection(Packet packet) {
        if (packet.hasAllFlags(Flag.SYN)) return true;
        Connection conn = this.getConnection();
        if (conn == null) return false;
        if (packet.hasAllFlags(Flag.ACK)) return true;
        return packet.getOrigin().equals(conn.getConnectedNode())
                && packet.getDestination().equals(conn.getConnectionSource()
        );
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

        /*
        if (packet.hasAllFlags(Flag.ACK)) {
            //if (this.connection != null) this.connection.update(packet);
            //System.out.println("creating connection");
            this.setConnection(new Connection(
                    this,
                    packet.getOrigin(),
                    packet.getAcknowledgmentNumber() - 1,
                    packet.getSequenceNumber())
            );
            this.outputBuffer = new SlidingWindow(this.getThisReceivingWindowCapacity(), this.connection, PACKET_COMPARATOR);
            this.inputBuffer = new SelectiveRepeat(this.getThisReceivingWindowCapacity(), this.connection, PACKET_COMPARATOR);


            //this.logger.log(Level.INFO, () -> "connection established with: " + this.getConnection());
            return false;
        }

         */
        return false;
    }

    public boolean seriousLossDetected() {
        try {
            return this.getSendingWindow().isSeriousLossDetected();
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public boolean canRetransmit(Packet packet) {
        try {
            return this.getSendingWindow().canRetransmit(packet);
        } catch (IllegalAccessException e) {
            //Retransmission should not happen if there is no SendingWindow
            return false;
        }
    }

    private void ack(Packet packet) {
        assert packet != null : "Packet is null";
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

    public Packet trySend() {
        if (this.outputBuffer instanceof SendingWindow) {
            SendingWindow sendingWindow = (SendingWindow) this.outputBuffer;

            if (sendingWindow.isQueueEmpty()) return null;
            if (sendingWindow.isWaitingForAck()) {
                return null;
            }

            Packet packetToSend = sendingWindow.send();
            assert packetToSend != null;
            this.route(packetToSend);
            return packetToSend;
        }
        return null;
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
}
