package org.example.protocol.window;

import org.example.data.Packet;
import org.example.util.PacketTimeout;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicWindow extends ArrayBlockingQueue<WindowEntry> implements Window {

    private final Logger logger = Logger.getLogger(BasicWindow.class.getName());
    private int timerDuration;

    public BasicWindow(int windowSize, int timerDuration) {
        //todo - test for both true and false in the fair argument
        super(windowSize, true);
        this.timerDuration = timerDuration;
    }

    @Override
    public boolean waitingForAck() {
        return super.remainingCapacity() == 0;
    }

    @Override
    public void retransmit(Packet Packet) {
        //todo - tror ikke denne er nødvendig
    }

    @Override
    public void add(Packet packet) {
        PacketTimeout packetTimeout = new PacketTimeout(this.timerDuration, packet);
        WindowEntry entry = new WindowEntry(packet, packetTimeout);
        boolean added = this.offer(entry);
        if (!added) throw new IllegalStateException("this Window could not accept the entry given");

    }

    @Override
    public void ackReceived(Packet ack) {
        //todo - test om du må sjekke om pakken er et ack
        for (WindowEntry entry : this) {
            if (entry.getPacketTimeout().isActive()){
                boolean isAckOnPacket = entry.getPacket().getSequenceNumber() == ack.getAcknowledgmentNumber();
                if (isAckOnPacket){
                    this.remove(entry);
                    return;
                }
            }
        }
    }

    @Override
    public Packet getPacketToSend() {
        for (WindowEntry entry : this) {
            if (entry.getPacketTimeout().isActive()) continue;

            //todo - må du ta hensyn til packets der timeren har gått ut. dvs. status.DONE
            entry.getPacketTimeout().start();
            return entry.getPacket();
        }
        logger.log(Level.INFO, "there are no packets in the window or all packets are waiting for ACK");
        return null;
    }

    @Override
    public void updateTimers() {
        for (WindowEntry entry : this) {
            entry.getPacketTimeout().decrement();
            if (entry.getPacketTimeout().isDone()){
                retransmit(entry.getPacket());
            }
        }
    }

    @Override
    public int windowSize() {
        return super.size() + super.remainingCapacity();
    }
}
