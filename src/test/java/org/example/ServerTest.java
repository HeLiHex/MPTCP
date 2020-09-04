package org.example;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {

    Server server;

    @Before
    public void setup(){
        server = new Server();
    }

    @After
    public void teardown(){
        server.close();
    }

    @Test
    public void getPORTReturnsPortTest(){
        Assert.assertEquals(6666, server.getPort());
    }

    @Test
    public void serverPortIsValidPortTest(){
        int port = server.getPort();
        Assert.assertTrue(port > 0 && port < 65536);
    }

    @Test
    public void getHOST_ADDRESSReturnsPortTest(){
        Assert.assertTrue(server.getHostAddress() instanceof String);
    }

    @Test
    public void serverIsBoundCorrectly(){
        Assert.assertEquals("localhost", server.getHostAddress());
        Assert.assertEquals(6666, server.getPort());
    }

}

