package org.example.protocol;

import org.example.network.Router;
import org.example.data.Packet;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class RoutableTest {

    private static final Random RANDOM_GENERATOR = new Random();


    @Test
    public void setRandomCostSetsCost(){
        Router r = new Router(1, RANDOM_GENERATOR);
        Assert.assertNotEquals(r.getCost(), 0);
    }

    @Test
    public void setRandomCostSetsPositiveCostAndNotZero(){
        Router r = new Router(1, RANDOM_GENERATOR);
        Assert.assertTrue(r.getCost() > 0);
    }

    @Test
    public void setRandomCostIsRandom(){
        int size = 100;
        int[] listOfCosts = new int[size];
        for (int i = 0; i < listOfCosts.length; i++) {
            Router r = new Router(1, RANDOM_GENERATOR);
            listOfCosts[i] = r.getCost();
        }
        Arrays.sort(listOfCosts);
        for (int i = 20; i < listOfCosts.length; i+=20) {
            Assert.assertFalse("The method is not random", listOfCosts[i] == listOfCosts[i-20]);
        }
    }


    @Test
    public void routingPacketRoutsItToItsDestinationStraitLine(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

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

        String msg = "hello på deg";
        client.route(new Packet(msg, server));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String receivedMsg = server.receive().getMsg();
        Assert.assertEquals(receivedMsg,msg);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationCircleGraph(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addNeighbour(r1);
        client.addNeighbour(r2);
        r1.addNeighbour(r3);
        r2.addNeighbour(r4);
        server.addNeighbour(r3);
        server.addNeighbour(r4);

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

        String msg = "hello på deg";
        client.route(new Packet(msg, server));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String receivedMsg = server.receive().getMsg();
        Assert.assertEquals(receivedMsg,msg);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationWithCycle(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addNeighbour(r1);
        r1.addNeighbour(r2);
        r1.addNeighbour(r3);
        r2.addNeighbour(r3);
        server.addNeighbour(r3);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        server.updateRoutingTable();

        r1.start();
        r2.start();
        r3.start();

        String msg = "hello på deg";
        client.route(new Packet(msg, server));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String receivedMsg = server.receive().getMsg();
        Assert.assertEquals(receivedMsg,msg);
    }

    @Test
    public void routingPacketRoutsItToItsDestinationWithDeadEnd(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addNeighbour(r1);
        r1.addNeighbour(r2);
        r1.addNeighbour(r4);
        r2.addNeighbour(r3);
        server.addNeighbour(r4);

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

        String msg = "hello på deg";
        client.route(new Packet(msg, server));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String receivedMsg = server.receive().getMsg();
        Assert.assertEquals(receivedMsg,msg);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationForrest(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        //tree one
        client.addNeighbour(r1);
        r1.addNeighbour(server);

        //tree two
        r2.addNeighbour(r3);
        r3.addNeighbour(r4);

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

        String msg = "hello på deg";
        client.route(new Packet(msg, server));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String receivedMsg = server.receive().getMsg();
        Assert.assertEquals(receivedMsg,msg);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationWithUnconnectedNode(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addNeighbour(r1);
        r1.addNeighbour(r2);
        r2.addNeighbour(r3);
        server.addNeighbour(r3);

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

        String msg = "hello på deg";
        client.route(new Packet(msg, server));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String receivedMsg = server.receive().getMsg();
        Assert.assertEquals(receivedMsg,msg);
    }


    @Test
    public void routingPacketRoutsItToItsDestinationCrazyGraph(){
        BasicTCP client = new BasicTCP(RANDOM_GENERATOR);
        Router r1 = new Router(100, RANDOM_GENERATOR);
        Router r2 = new Router(100, RANDOM_GENERATOR);
        Router r3 = new Router(100, RANDOM_GENERATOR);
        Router r4 = new Router(100, RANDOM_GENERATOR);
        Router r5 = new Router(100, RANDOM_GENERATOR);
        Router r6 = new Router(100, RANDOM_GENERATOR);
        Router r7 = new Router(100, RANDOM_GENERATOR);
        Router r8 = new Router(100, RANDOM_GENERATOR);
        Router r9 = new Router(100, RANDOM_GENERATOR);
        Router r10 = new Router(100, RANDOM_GENERATOR);
        Router r11 = new Router(100, RANDOM_GENERATOR);
        Router r12 = new Router(100, RANDOM_GENERATOR);
        BasicTCP server = new BasicTCP(RANDOM_GENERATOR);

        client.addNeighbour(r1);
        client.addNeighbour(r2);
        r1.addNeighbour(r3);
        r2.addNeighbour(r3);
        r3.addNeighbour(r4);
        r3.addNeighbour(r9);
        r4.addNeighbour(r5);
        r4.addNeighbour(r6);
        r5.addNeighbour(r6);
        r6.addNeighbour(r9);
        r6.addNeighbour(r7);
        r7.addNeighbour(r8);
        r9.addNeighbour(r10);
        r10.addNeighbour(r11);
        r11.addNeighbour(r12);
        r12.addNeighbour(server);

        client.updateRoutingTable();
        r1.updateRoutingTable();
        r2.updateRoutingTable();
        r3.updateRoutingTable();
        r4.updateRoutingTable();
        r5.updateRoutingTable();
        r6.updateRoutingTable();
        r7.updateRoutingTable();
        r8.updateRoutingTable();
        r9.updateRoutingTable();
        r10.updateRoutingTable();
        r11.updateRoutingTable();
        r12.updateRoutingTable();
        server.updateRoutingTable();

        r1.start();
        r2.start();
        r3.start();
        r4.start();
        r5.start();
        r6.start();
        r7.start();
        r8.start();
        r9.start();
        r10.start();
        r11.start();
        r12.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String msg = "hello på deg";
        client.route(new Packet(msg, server));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String receivedMsg = server.receive().getMsg();
        Assert.assertEquals(receivedMsg, msg);
    }



}
