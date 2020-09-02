package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerClientIntegrationTest {

    private Server server;
    private Client client;


    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();



    @Before
    public void setUp(){
        this.server = new Server();
        this.client = new Client();
    }


    @Test(expected = Test.None.class)
    public void connectClientToServerTest() {
        String host = this.server.getHOST_ADDRESS();
        int port = this.server.getPORT();
        this.client.setHostAndPort(host, port);

        this.server.start();
        this.client.start();

        //todo - this test does not test anything

    }

}