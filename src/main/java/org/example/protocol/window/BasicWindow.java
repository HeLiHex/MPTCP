package org.example.protocol.window;

import org.example.data.Packet;
import org.example.protocol.BasicTCP;
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

    }

    @Override
    public Packet getPacketToSend() {
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
