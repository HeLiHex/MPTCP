package org.example.protocol.window;

import org.example.data.Packet;
import org.example.util.PacketTimeout;

public class WindowEntry {

    private Packet packet;
    private PacketTimeout packetTimeout;


    public WindowEntry(Packet packet, PacketTimeout packetTimeout) {
        if (!packetTimeout.getPacket().equals(packet)){
            throw new IllegalArgumentException("The PacketTimer must be based on the Packet given");
        }
        this.packet = packet;
        this.packetTimeout = packetTimeout;
    }


    public Packet getPacket() {
        return this.packet;
    }

    public PacketTimeout getPacketTimeout() {
        return this.packetTimeout;
    }

}
