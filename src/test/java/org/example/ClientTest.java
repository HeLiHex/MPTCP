package org.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {



    @Test
    public void clientConstructorTest(){
        Client client = new Client();
        Assert.assertTrue(client instanceof Client);
    }

    @Test
    public void clientConstructorWithHostAndPortTest(){
        String testHost = "testhost";
        int testPort = 1313;
        Client client = new Client(testHost, testPort);
        Assert.assertTrue(client instanceof Client);
        Assert.assertEquals(client.getHost(), testHost);
        Assert.assertTrue(client.getPort() == testPort);
    }

    @Test
    public void setHostAndPortSetsHostAndPortCorrectlyTest(){
        Client client = new Client();
        String testHost = "testhost";
        int testPort = 1313;

        client.setHostAndPort(testHost, testPort);

        Assert.assertTrue(client instanceof Client);
        Assert.assertEquals(client.getHost(), testHost);
        Assert.assertTrue(client.getPort() == testPort);
    }


}
