package org.universe.queue.test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.universe.queue.Message;
import org.universe.queue.SimpleQueue;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Test_SimpleQueue {

    final static Object UniqueProcessId = UUID.randomUUID().toString();
    private SimpleQueue queue;
    Random rnd = new Random(1);
    static AtomicInteger counter = new AtomicInteger();
    private String queueName;

    @Before
    public void before() throws Exception {
        queue = new SimpleQueue(EnvQueue.createDataSource());
        queueName = "Test-" + (counter.incrementAndGet()) + "-" + UniqueProcessId;
    }

    @Test
    public void _1_Non_Existed_Message_Is_Absent() throws Exception {
        Message.Status status = queue.getMessageStatus("no such message");
        Assert.assertTrue("Message 'no such message' is absent", status == null);
    }

    @Test
    public void _2_Publish_Ack_() throws Exception {

        List<Integer> messageSizes = Arrays.asList(0, 1, 16, 55, 1024, 1000000, 10 * 1024 * 1024);
        System.out.print("Message Size: ");

        for(int sz : messageSizes)
        {
            System.out.print(sz + " ");
            String key = "Key-" + UUID.randomUUID().toString();
            byte[] expectedData = new byte[sz];
            rnd.nextBytes(expectedData);
            queue.publish(queueName, key, expectedData);

            {
                Message.Status status = queue.getMessageStatus(key);
                Assert.assertNotNull("After ack status is present", status);
                Assert.assertNull("After publish AckDate is null", status.getAckDate());
                Assert.assertTrue("After publish message is Unlocked", !status.isLocked());
            }


            Message msg = queue.nextDelivery(queueName);
            Assert.assertNotNull(msg);
            Assert.assertArrayEquals(expectedData, msg.getMessage());
            Assert.assertTrue("HandlersCount === 1", msg.getHandlersCount() == 1);

            {
                Message.Status status = queue.getMessageStatus(key);
                Assert.assertNotNull("During processing status is present", status);
                Assert.assertNull("During processing AckDate is null", status.getAckDate());
                Assert.assertTrue("During processing message is Locked", status.isLocked());
            }


            queue.ack(queueName, msg.getId());
            {
                Message.Status status = queue.getMessageStatus(key);
                Assert.assertNotNull("After ack status is present", status);
                Assert.assertNotNull("After ack AckDate is present", status.getAckDate());
                Assert.assertTrue("After ack message is Unlocked", !status.isLocked());
            }
        }

        System.out.println();

    }

    @After
    public void after() throws Exception {
        queue.purgeOldMessages(queueName, 0);

        System.out.println(SimpleQueue.LocalStat.toConsoleTable());
    }

}
