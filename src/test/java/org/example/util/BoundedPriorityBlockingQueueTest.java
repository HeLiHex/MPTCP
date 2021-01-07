package org.example.util;

import org.example.data.Packet;
import org.example.data.PacketBuilder;
import org.junit.Assert;
import org.junit.Test;

public class BoundedPriorityBlockingQueueTest {


    @Test
    public void bothConstructorsWorkTest(){
        BoundedQueue bq1 = new BoundedPriorityBlockingQueue(10);
        BoundedQueue<Packet> bq2 = new BoundedPriorityBlockingQueue<>(10, (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber());
        Assert.assertTrue(bq1 instanceof BoundedPriorityBlockingQueue);
        Assert.assertTrue(bq2 instanceof BoundedPriorityBlockingQueue);
    }


    @Test
    public void offerInsertsOrdered(){
        BoundedPriorityBlockingQueue<Packet> boundedPriorityBlockingQueue =
                new BoundedPriorityBlockingQueue<>(5, (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber());

        Packet one = new PacketBuilder()
                .withSequenceNumber(1)
                .build();
        Packet two = new PacketBuilder()
                .withSequenceNumber(2)
                .build();
        Packet three = new PacketBuilder()
                .withSequenceNumber(3)
                .build();
        Packet four = new PacketBuilder()
                .withSequenceNumber(4)
                .build();
        Packet five = new PacketBuilder()
                .withSequenceNumber(5)
                .build();

        boundedPriorityBlockingQueue.offer(three);
        boundedPriorityBlockingQueue.offer(four);
        boundedPriorityBlockingQueue.offer(one);
        boundedPriorityBlockingQueue.offer(five);
        boundedPriorityBlockingQueue.offer(two);

        Assert.assertEquals(one, boundedPriorityBlockingQueue.poll());
        Assert.assertEquals(two, boundedPriorityBlockingQueue.poll());
        Assert.assertEquals(three, boundedPriorityBlockingQueue.poll());
        Assert.assertEquals(four, boundedPriorityBlockingQueue.poll());
        Assert.assertEquals(five, boundedPriorityBlockingQueue.poll());
        Assert.assertEquals(null, boundedPriorityBlockingQueue.poll());
    }

    @Test
    public void isFullWorksReturnsTrueIfFullTest(){
        BoundedPriorityBlockingQueue<Packet> boundedPriorityBlockingQueue =
                new BoundedPriorityBlockingQueue<>(5, (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber());

        Packet one = new PacketBuilder()
                .withSequenceNumber(1)
                .build();
        Packet two = new PacketBuilder()
                .withSequenceNumber(2)
                .build();
        Packet three = new PacketBuilder()
                .withSequenceNumber(3)
                .build();
        Packet four = new PacketBuilder()
                .withSequenceNumber(4)
                .build();
        Packet five = new PacketBuilder()
                .withSequenceNumber(5)
                .build();

        boundedPriorityBlockingQueue.offer(one);
        boundedPriorityBlockingQueue.offer(two);
        boundedPriorityBlockingQueue.offer(three);
        boundedPriorityBlockingQueue.offer(four);
        boundedPriorityBlockingQueue.offer(five);

        Assert.assertTrue(boundedPriorityBlockingQueue.isFull());
    }


    @Test
    public void isFullWorksReturnsFalseIfNotFullTest(){
        BoundedPriorityBlockingQueue<Packet> boundedPriorityBlockingQueue =
                new BoundedPriorityBlockingQueue<>(5, (packet, t1) -> packet.getSequenceNumber() - t1.getSequenceNumber());

        Packet one = new PacketBuilder()
                .withSequenceNumber(1)
                .build();
        Packet two = new PacketBuilder()
                .withSequenceNumber(2)
                .build();
        Packet three = new PacketBuilder()
                .withSequenceNumber(3)
                .build();
        Packet four = new PacketBuilder()
                .withSequenceNumber(4)
                .build();

        boundedPriorityBlockingQueue.offer(one);
        boundedPriorityBlockingQueue.offer(two);
        boundedPriorityBlockingQueue.offer(three);
        boundedPriorityBlockingQueue.offer(four);

        Assert.assertFalse(boundedPriorityBlockingQueue.isFull());
    }

}
