package org.example.network;

import org.example.network.address.Address;
import org.example.network.address.UUIDAddress;
import org.junit.Assert;
import org.junit.Test;

public class AddressTest {

    @Test
    public void createAddressWorksTest() {
        Address address = new UUIDAddress();
        Assert.assertTrue(address instanceof Address);
    }

}
