package org.example.protocol;

import org.example.network.Router;
import org.example.protocol.util.Packet;
import org.junit.Assert;
import org.junit.Test;

public class BasicTCPTest {


    @Test
    public void sendEnqueuesPackageToOutputBufferTest() {
        TCP tcp = new BasicTCP();
        String msg = "test";
        tcp.send(new Packet(msg));

        Packet getPacketInBuffer = ((BasicTCP) tcp).getOutputBuffer().peek();
        Assert.assertEquals(getPacketInBuffer.getMsg(), msg);

    }

    @Test
    public void trySendWaitsForAck() {
        TCP tcp = new BasicTCP();
        String msg = "test";
        tcp.send(new Packet(msg));

        //Packet ack = ((BasicTCP)tcp).trySend();
        //Assert.assertEquals(ack.getMsg(), "ACK");
    }


    @Test
    public void main() {
        BasicTCP client = new BasicTCP();
        Router r1 = new Router();
        Router r2 = new Router();
        Router r3 = new Router();
        Router r4 = new Router();
        BasicTCP server = new BasicTCP();

        client.addNeighbour(r1);
        r1.addNeighbour(r2);
        r2.addNeighbour(r3);
        r3.addNeighbour(r4);
        r4.addNeighbour(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        server.updateRoutingTable();

        r1.start();
        r2.start();
        r3.start();
        r4.start();

        client.route(new Packet("hello på deg"));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(server.receive());



/**
 Packet packet = new Packet("Hello server! this is client", server);

 client.send(packet); // legger i buffer
 Packet answer = client.trySend(); // prøver å sende og venter på ack
 System.out.println(answer.getMsg());
 **/
    }


}
