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
        Assert.assertEquals(testHost, client.getHost());
        Assert.assertEquals(testPort, client.getPort());
    }


    @Test
    public void setHostAndPortSetsHostAndPortCorrectlyIfValidParamsTest(){
        Client client = new Client();
        String testHost = "testhost";
        int testPort = 1313;

        client.setHostAndPort(testHost, testPort);

        Assert.assertTrue(client instanceof Client);
        Assert.assertEquals(testHost, client.getHost());
        Assert.assertEquals(testPort, client.getPort());
    }

    @Test
    public void setHostSetsHostTest(){
        String testHost = "testehost";
        Client client = new Client();
        client.setHost(testHost);

        String errorMsg = "Wrong host set";
        Assert.assertEquals(errorMsg, testHost, client.getHost());
    }

    @Test
    public void setPortWithValidPortTest(){
        int validPort = 1337;
        Client client = new Client();
        client.setPort(validPort);

        String errorMsg = "Valid port was not accepted";
        Assert.assertEquals(errorMsg, validPort, client.getPort());
    }

    @Test
    public void setPortWithInvalidPortTest(){
        int invalidPort = -1;
        Client client = new Client();
        client.setPort(invalidPort);

        String errorMsg = "Invalid port was accepted";
        Assert.assertNotEquals(errorMsg, invalidPort, client.getPort());
    }

    @Test
    public void isValidPortReturnsTrueIfValidPortIsGivenTest(){
        int validPort = 1337;
        String errorMsg = "Valid port was not accepted";
        Assert.assertTrue(errorMsg, new Client().isValidPort(validPort));
    }

    @Test
    public void isValidPortReturnsFalseIfInvalidPortIsGivenTest(){
        int invalidPort = 0;
        String errorMsg = "Invalid port was accepted";
        Assert.assertFalse(errorMsg, new Client().isValidPort(invalidPort));
    }





}
