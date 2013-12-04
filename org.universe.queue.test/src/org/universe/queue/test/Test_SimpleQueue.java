package org.universe.queue.test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.universe.ConsoleTable;
import org.universe.DateCalc;
import org.universe.System6;
import org.universe.jcl.Lazy;
import org.universe.jcl.Stress;
import org.universe.queue.Message;
import org.universe.queue.SimpleQueue;
import org.universe.queue.SimpleQueueDataSourceFactory;
import org.universe.test.TestEnv;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Test_SimpleQueue {

    final static Object UniqueProcessId = UUID.randomUUID().toString();
    private SimpleQueue queue;
    Random rnd = new Random(1);
    static AtomicInteger counter = new AtomicInteger();
    private String queueName;

    static Lazy<List<SimpleQueueDataSourceFactory>> DbList =
            new Lazy<List<SimpleQueueDataSourceFactory>>(Lazy.Mode.ExecutionAndPublication) {
                @Override
                protected List<SimpleQueueDataSourceFactory> initialValue() throws Exception {
                    ArrayList<SimpleQueueDataSourceFactory> ret = new ArrayList<SimpleQueueDataSourceFactory>();

                    if (TestEnv.getScope() == TestEnv.Scope.DEPLOY)
                    {
                        ret.add(EnvQueue.msSql());
                        ret.add(EnvQueue.mysql());
                    }

                    ret.add(EnvQueue.derbyMemory());
                    ret.add(EnvQueue.sqliteDisk());
                    ret.add(EnvQueue.derbyDisk());
                    // ret.add(EnvQueue.sqliteMem());
                    return ret;

                }
            };

    @Before
    public void before() throws Exception {
        // queue = new SimpleQueue(EnvQueue.createDataSource());
        queueName = "Test-" + (counter.incrementAndGet()) + "-" + UniqueProcessId;
    }

    @Test
    public void _1_Non_Existed_Message_Is_Absent() throws Exception {
        queue = new SimpleQueue(EnvQueue.derbyMemory());
        Message.Status status = queue.getMessageStatus("no such message");
        Assert.assertTrue("Message 'no such message' is absent", status == null);
    }

    @Test
    public void _2_Non_Existed_Message_Is_Absent_All_DBs() throws Exception {
        for(SimpleQueueDataSourceFactory ds : DbList.get())
        {
            System.out.println("Test message status only on " + ds.url);
            queue = new SimpleQueue(ds);
            queue.deleteQueue(queueName);
            Message.Status status = queue.getMessageStatus("no such message");
            Assert.assertTrue("Message 'no such message' is absent", status == null);
            queue.purgeOldMessages(queueName, -1000000000L);
        }
    }

    @Test
    public void _3_Publish_Ack_() throws Exception {

        ConsoleTable report = new ConsoleTable("Size", "DB", "Status", "Publish", "Status", "Delivery", "Ack");
        for(SimpleQueueDataSourceFactory ds : DbList.get())
        {
            System.out.println("Test messages lifecycle on " + ds.url);
            queue = new SimpleQueue(ds);
            List<Integer> messageSizes = Arrays.asList(1, 16, 55, 1024, 1000000, 10 * 1024 * 1024);
            System.out.print("Message Size: ");

            for(int sz : messageSizes)
            {
                System.out.print(sz + " ");
                String key = "Key-" + UUID.randomUUID().toString();
                byte[] expectedData = new byte[sz];
                rnd.nextBytes(expectedData);

                long timeZeroStatus = System.nanoTime();
                queue.getMessageStatus(UUID.randomUUID().toString());
                timeZeroStatus = System.nanoTime() - timeZeroStatus;

                // PUBLISH
                long timePublish = System.nanoTime();
                queue.publish(queueName, key, expectedData);
                timePublish = System.nanoTime() - timePublish;

                long timeStatus = System.nanoTime();
                {
                    Message.Status status = queue.getMessageStatus(key);
                    timeStatus = System.nanoTime() - timeStatus;

                    Assert.assertNotNull("After ack status is present", status);
                    Assert.assertNull("After publish AckDate is null", status.getAckDate());
                    Assert.assertTrue("After publish message is Unlocked", !status.isLocked());
                }


                // DELIVERY
                long timeDelivery = System.nanoTime();
                Message msg = queue.nextDelivery(queueName);
                timeDelivery = System.nanoTime() - timeDelivery;

                Assert.assertNotNull(msg);
                Assert.assertArrayEquals(expectedData, msg.getMessage());
                Assert.assertTrue("HandlersCount === 1", msg.getHandlersCount() == 1);

                {
                    Message.Status status = queue.getMessageStatus(key);
                    Assert.assertNotNull("During processing status is present", status);
                    Assert.assertNull("During processing AckDate is null", status.getAckDate());
                    Assert.assertTrue("During processing message is Locked", status.isLocked());
                }


                // ACK
                long timeAck = System.nanoTime();
                queue.ack(queueName, msg.getId());
                timeAck = System.nanoTime() - timeAck;

                {
                    Message.Status status = queue.getMessageStatus(key);
                    Assert.assertNotNull("After ack status is present", status);
                    Assert.assertNotNull("After ack AckDate is present", status.getAckDate());
                    Assert.assertTrue("After ack message is Unlocked", !status.isLocked());
                }

                report
                        .put(sz)
                        .put(queue.getDialect())
                        .put(DateCalc.NanosecToString(timeZeroStatus))
                        .put(DateCalc.NanosecToString(timePublish))
                        .put(DateCalc.NanosecToString(timeStatus))
                        .put(DateCalc.NanosecToString(timeDelivery))
                        .put(DateCalc.NanosecToString(timeAck))
                        .newRow();
            }

            System.out.println();
        }

        System.out.println(report);

    }

    @Test
    public void _4_Publish_Ack_Under_Stress() throws Exception {

        ConsoleTable report = new ConsoleTable("Size", "DB", "Status", "Publish", "Status", "Delivery", "Ack");
        for(SimpleQueueDataSourceFactory ds : DbList.get())
        {
            System.out.println("Test messages lifecycle on " + ds.url);
            queue = new SimpleQueue(ds);
            List<Integer> messageSizes = Arrays.asList(1); // , 16, 55, 1024, 1000000, 10 * 1024 * 1024);

            for(final int sz : messageSizes)
            {
                System.out.println("Message Size: " + sz);


                Stress.LoopBody body = new Stress.LoopBody() {
                    @Override
                    public void run() throws Exception {

                        String key = "Key-" + UUID.randomUUID().toString();
                        byte[] expectedData = new byte[sz];
                        rnd.nextBytes(expectedData);
                        // 1
                        Message.Status stat1 = queue.getMessageStatus(UUID.randomUUID().toString());
                        Assert.assertNull(stat1);
                        // 2
                        boolean isNew = queue.publish(queueName, key, expectedData);
                        Assert.assertTrue(isNew);
                        // 3
                        Message.Status stat2 = queue.getMessageStatus(key);
                        Assert.assertNotNull(stat2);
                        Assert.assertEquals(key, stat2.getOptionalKey());
                        // 4
                        Message msg = queue.nextDelivery(queueName);
                        if (msg != null || true)
                        {
                            if (rnd.nextBoolean())
                                queue.ack(queueName, msg.getId());
                            else
                                queue.postpone(queueName, msg.getId(), null);
                        }
                    }
                };

                measure(body, "Lifecycle " + sz + " at " + ds.url, 4000);
            }
        }
    }

    private void measure(Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        System.out.print(caption + ": ");
        Stress.Heat(run);
        List<Integer> threads = Arrays.asList(1, 2, 6, 12);
        ConsoleTable tbl = Stress.CreateReport();
        for(int t : threads)
        {
            System.out.print(t + " ");
            Stress res = Stress.Measure(TestEnv.scaleDuration(duration), 1, t, run);
            res.WriteToReport(tbl);
        }

        String ls = System6.lineSeparator();
        System.out.println(ls + ls + caption + ls + tbl);
    }


    // @After
    public void after() throws Exception {
        queue.purgeOldMessages(queueName, 0);

        System.out.println(SimpleQueue.LocalStat.toConsoleTable());
    }

}
