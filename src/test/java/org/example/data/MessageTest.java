package org.example.data;

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {

    @Test
    public void createMessageWorksTest(){
        Message message = new Message("test");
        Assert.assertTrue(message instanceof Message);
    }

    @Test
    public void stringInMessageIsCorrectTest(){
        String s1 = "test";
        Message message1 = new Message(s1);
        Assert.assertEquals(s1, message1.toString());

        String s2 = "test12345";
        Message message2 = new Message(s2);
        Assert.assertEquals(s2, message2.toString());
    }

    @Test
    public void sizeOfMessageIsCorrectTest(){
        String s1 = "test";
        Message message1 = new Message(s1);
        Assert.assertEquals(s1.length(), message1.size());

        String s2 = "test12345";
        Message message2 = new Message(s2);
        Assert.assertEquals(s2.length(), message2.size());
    }


    @Test
    public void messagesWithEqualStringsAreEqualTest(){
        String s = "test";
        Message message1 = new Message(s);
        Message message2 = new Message(s);
        Assert.assertEquals(message1, message2);
    }


}
