package org.example.protocol;

import org.example.network.Address;
import org.junit.Assert;
import org.junit.Test;

public class AddressTest {

    @Test
    public void createAddressWorksTest(){
        Address address = new Address("test", 6666);
        Assert.assertTrue(address instanceof Address);
    }

}
