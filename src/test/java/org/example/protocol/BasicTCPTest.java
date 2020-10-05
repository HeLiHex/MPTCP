package org.example.protocol;

import org.example.network.Router;
import org.example.protocol.util.Packet;
import org.junit.Assert;
import org.junit.Test;

public class BasicTCPTest {



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
        Router r1 = new Router(100);
        Router r2 = new Router(100);
        Router r3 = new Router(100);
        Router r4 = new Router(100);
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

        client.route(new Packet("hello på deg", server));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String msg = server.receive().getMsg();
        System.out.println(msg);
        Assert.assertEquals(msg,"hello på deg");

    }


}
