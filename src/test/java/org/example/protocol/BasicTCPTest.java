package org.example.protocol;

import org.example.data.Packet;
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

        client.addNeighbour(server);
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

        client.addNeighbour(server);

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
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(server.receive(), packet);

    }




}
