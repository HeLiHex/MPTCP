package org.example.protocol;

import org.example.data.Packet;
import org.example.network.Address;
import org.example.network.Channel;
import org.example.network.interfaces.Endpoint;
import org.example.network.interfaces.NetworkNode;

import java.util.List;

public class NewTCP extends NewAbstractTCP implements TCP, Endpoint {

    private final Endpoint endpoint;

    public NewTCP(Endpoint endpoint) {
        this.endpoint = endpoint;
    }


//TODO - dette er en god ide, men det er ikke klart helt enda

    //-------------------AbstractTCP-----------------

    @Override
    public Channel getChannel() {
        if (this.isConnected()) return this.getPath(this.getConnection().getConnectedNode());
        return this.getChannel(0);
    }

    private Channel getChannel(int channelIndex) {
        return this.getChannels().get(channelIndex);
    }

    //---------------------Endpoint------------------------
    @Override
    public Packet dequeueOutputBuffer() {
        return this.endpoint.dequeueOutputBuffer();
    }

    @Override
    public boolean enqueueOutputBuffer(Packet packet) {
        return this.endpoint.enqueueOutputBuffer(packet);
    }

    @Override
    public boolean outputBufferIsEmpty() {
        return this.endpoint.outputBufferIsEmpty();
    }

    @Override
    public int outputBufferSize() {
        return this.endpoint.outputBufferSize();
    }

    @Override
    public void updateRoutingTable() {
        this.endpoint.updateRoutingTable();;
    }

    @Override
    public void route(Packet packet) {
        this.endpoint.route(packet);
    }

    @Override
    public long processingDelay() {
        return this.endpoint.processingDelay();
    }

    @Override
    public List<Channel> getChannels() {
        return this.endpoint.getChannels();
    }

    @Override
    public void addChannel(NetworkNode node) {
        this.endpoint.addChannel(node);
    }

    @Override
    public Address getAddress() {
        return this.endpoint.getAddress();
    }

    @Override
    public Packet peekInputBuffer() {
        return this.endpoint.peekInputBuffer();
    }

    @Override
    public Packet dequeueInputBuffer() {
        return this.endpoint.dequeueInputBuffer();
    }

    @Override
    public boolean enqueueInputBuffer(Packet packet) {
        return this.endpoint.enqueueInputBuffer(packet);
    }

    @Override
    public boolean inputBufferIsEmpty() {
        return this.endpoint.inputBufferIsEmpty();
    }

    @Override
    public Channel getPath(NetworkNode destination) {
        return this.endpoint.getPath(destination);
    }

    @Override
    public void run() {
        this.endpoint.run();
    }
}
