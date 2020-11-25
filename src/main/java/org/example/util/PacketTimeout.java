package org.example.util;

import org.example.data.Packet;

public class PacketTimeout extends Timeout {

    private Packet packet;

    public PacketTimeout(int duration, Packet packet) {
        super(duration);
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public void stopTimeout(Packet ack){
        if (ack.getAcknowledgmentNumber() == packet.getSequenceNumber()){
            this.deactivate();
        }
    }
}
