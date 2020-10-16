package org.example.protocol;

import org.example.data.Packet;
import org.example.network.Router;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static java.lang.Thread.sleep;

public class BasicTCPTest {


    private static final Random RANDOM_GENERATOR = new Random();

    @Test
    public void connectToEndpointTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);
        client.updateRoutingTable();

        server.start();

        client.connect(server);

        Assert.assertEquals(server, client.getConnectedNode());
        Assert.assertEquals(client, server.getConnectedNode());
    }

    @Test
    public void connectThenSendMsgWorksTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addChannel(server);

        server.start();

        client.connect(server);

        String msg = "hello på do!";
        Packet packet = new Packet.PacketBuilder()
                .withOrigin(client)
                .withMsg(msg)
                .withDestination(server)
                .build();

        client.send(packet);

        try {
            sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(server.receive(), packet);

    }


    @Test
    public void connectThenSendMsgOverMultipleNodesLineWorksTest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);

        client.addChannel(r1);
        r1.addChannel(r2);
        r2.addChannel(r3);
        r3.addChannel(r4);
        r4.addChannel(server);

        r1.start();
        r2.start();
        r3.start();
        r4.start();
        server.start();

        client.connect(server);

        String msg = "hello på do!";
        Packet packet = new Packet.PacketBuilder()
                .withOrigin(client)
                .withMsg(msg)
                .withDestination(server)
                .build();

        client.send(packet);

        try {
            sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(server.receive(), packet);

    }




}
