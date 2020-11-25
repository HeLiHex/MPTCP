package org.example.network;

import org.example.network.Address;
import org.junit.Assert;
import org.junit.Test;

public class AddressTest {

    @Test
    public void createAddressWorksTest() {
        Address address = new Address();
        Assert.assertTrue(address instanceof Address);
    }

}
