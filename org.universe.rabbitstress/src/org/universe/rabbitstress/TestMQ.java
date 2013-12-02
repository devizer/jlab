package org.universe.rabbitstress;

import com.rabbitmq.client.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.universe.ConsoleTable;
import org.universe.DateCalc;
import org.universe.System6;
import org.universe.jcl.ReliableThreadLocal;
import org.universe.jcl.Stress;
import org.universe.test.TestEnv;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TestMQ {

    final static Object UniqueProcessId = new Date().toString();
    final static AtomicLong TestsCounter = new AtomicLong();
    final String ConnectionString = "localhost";
    String QueueName;

    @Before
    public void before() throws IOException {
        long startAt = System.nanoTime();
        QueueName = "TestMQ-" + TestsCounter.incrementAndGet() + " (" + UniqueProcessId + ")";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConnectionString);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        boolean isDurable = true;
        boolean isExclusive = false;
        boolean isAutoDelete = true; // really?
        channel.queueDeclare(QueueName, isDurable, isExclusive, isAutoDelete, null);
        channel.close();
        connection.close();
        long duration = System.nanoTime() - startAt;
        System.out.println(String.format(
                "New '%s' queue created in %s secs",
                QueueName, DateCalc.NanosecToString(duration)
        ));

        keepAliveQueue();
    }

    // Keep autodelete queue alive until end of test
    private void keepAliveQueue() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConnectionString);
        Channel channel = factory.newConnection().createChannel();
        QueueingConsumer queueHolder = new QueueingConsumer(channel);
        channel.basicConsume(QueueName, true, "Holder of autodeleted queue", queueHolder);
    }

    @Test
    public void _1_Porduce_Short_Message() throws IOException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConnectionString);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        AtomicInteger count = new AtomicInteger(1);

        String s = "Hello World! " + count.getAndIncrement();
        channel.basicPublish("", QueueName, MessageProperties.PERSISTENT_TEXT_PLAIN, s.getBytes());
    }

    @Test
    public void _2_Produce_Many_Messages() throws IOException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConnectionString);
        final AtomicInteger count = new AtomicInteger(1);
        final Connection connection = factory.newConnection();

        final ReliableThreadLocal<Channel> channel = new ReliableThreadLocal<Channel>() {
            @Override
            protected Channel initialValue() throws IOException {
                return connection.createChannel();
            }
        };

        Stress.ThreadLocalInitializer init = new Stress.ThreadLocalInitializer() {
            @Override
            public void exec() throws Exception {
                channel.get();
            }
        };

        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() throws Exception {
                String s = "Hello World! " + count.getAndIncrement();
                Channel ch = channel.get();
                ch.basicPublish("", QueueName, MessageProperties.PERSISTENT_TEXT_PLAIN, s.getBytes());
            }
        };

        String caption = "Measure: Produce Many Messages";
        measure(init, run, caption, 10000);
    }

    @Test
    public void _3_Many_Consumers_And_Producers() throws Exception {

        List<Integer> config = Arrays.asList(
                // 1 CONSUMER
                16, 1,
                8,  1,
                4,  1,
                2,  1,
                1,  1,
                0,  1,
                // 4 CONSUMERS
                0,  4,
                1,  4,
                2,  4,
                4,  4,
                8,  4,
                16, 4,
                // 16 CONSUMERS
                0,  16,
                1,  16,
                2,  16,
                4,  16,
                8,  16,
                16, 16
        );

        // THREAD LOCAL PUBLISHER's CHANNEL
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConnectionString);
        final AtomicInteger count = new AtomicInteger(1);
        final Connection connection = factory.newConnection();

        final ReliableThreadLocal<Channel> publisherChannel = new ReliableThreadLocal<Channel>() {
            @Override
            protected Channel initialValue() throws IOException {
                return connection.createChannel();
            }
        };

        Stress.LoopBody producerLoopBody = new Stress.LoopBody() {
            @Override
            public void run() throws Exception {
                String s = "Hello World! " + count.getAndIncrement();
                Channel ch = publisherChannel.get();
                ch.basicPublish("", QueueName, MessageProperties.PERSISTENT_TEXT_PLAIN, s.getBytes());
            }
        };

        ConsoleTable report = new ConsoleTable("PRO", "CON", "Duration", "Sent", "Recieved", "Consumers", "Cons Terminated");

        System.out.print("Measure Many Producers*Consumers: ");
        for(int x = 0; x<config.size(); x+=2)
        {
            long startAt = System.nanoTime();
            int numProducers = config.get(x);
            int numConsumers = config.get(x+1);

            System.out.print("{" + numProducers + "*" + numConsumers + "} ");

            // START CONSUMERS, Single CHANNEL per all consumer
            ExecutorService consumersExecutor = Executors.newFixedThreadPool(numConsumers);
            Connection consumerConnection = factory.newConnection(consumersExecutor);
            final AtomicLong numRecievedMessages = new AtomicLong();
            // SINGLE CHANNEL PER ALL CONSUMERS
            final Channel consumerChannel = consumerConnection.createChannel();
            final Set<String> consumerThreads = Collections.synchronizedSet(new HashSet<String>());

            boolean autoAck = false;
            String consumerTag = "Auto Executed Consumer";
            consumerChannel.basicConsume(QueueName, autoAck, consumerTag, new DefaultConsumer(publisherChannel.get()) {

                @Override
                public void handleConsumeOk(String consumerTag) {
                    // only once per all
                    super.handleConsumeOk(consumerTag);
                    // System.out.println("::handleConsumeOk(" + consumerTag + ") at thread" + Thread.currentThread().getName());
                }

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    consumerThreads.add(Thread.currentThread().getName());
                    String suffix = " at thread " + Thread.currentThread().getName();
                    String message = new String(body);

                    if (Math.random() > 0.1) {
                        // ack only one
                        consumerChannel.basicAck(envelope.getDeliveryTag(), false);
                        // System.out.println(message + ": Handled" + suffix);
                    } else {
                        boolean needRequeue = Math.random() > 0.5;
                        consumerChannel.basicReject(envelope.getDeliveryTag(), needRequeue);
                        // System.out.println(message + ": " + (needRequeue ? "Requeued" : "Dropped") + suffix);
                    }

                    numRecievedMessages.incrementAndGet();
                }
            });

            // Run publishers for 10 seconds
            Stress stressReport = null;
            int duration = 4000;
            if (numProducers > 0)
            {
                stressReport = Stress.Measure(TestEnv.scaleDuration(duration), 1, numProducers, producerLoopBody);
                Assert.assertTrue("At least 1 message was published", stressReport.SuccessCount > 0);
                Assert.assertTrue("All messages are published", stressReport.SuccessCount == stressReport.Count);
            }
            else
            {
                Thread.sleep(TestEnv.scaleDuration(duration));
            }

            // Stop Consumers
            consumerChannel.basicCancel(consumerTag);
            // Shutdown thread pool
            consumersExecutor.shutdown();

            // What is termination time?
            long tns = System.nanoTime();
            boolean isTerminatedOk = false;
            {
                long ns = System.nanoTime();
                while(!isTerminatedOk)
                {
                    if (( isTerminatedOk = consumersExecutor.awaitTermination(10, TimeUnit.MILLISECONDS)))
                        break;

                    if (System.nanoTime() - ns > 10000000000L)
                        break;
                }
            }
            long terminatedIn = System.nanoTime() - tns;

            report  .put(numProducers)
                    .put(numConsumers)
                    .put(DateCalc.NanosecToString(System.nanoTime() - startAt))
                    .put(stressReport == null ? "-" : String.valueOf(stressReport.SuccessCount))
                    .put(numRecievedMessages.get())
                    .put(consumerThreads.size())
                    .put((isTerminatedOk ? "OK in ": "Not yet in ") + DateCalc.NanosecToString(terminatedIn) + " s")
                    .newRow();
        }

        String ls = System6.lineSeparator();
        System.out.println(ls + ls + "Many producers and consumers" + ls + report.toString() + ls);

    }



    @After
    public void after() throws IOException, InterruptedException {

        long startAt = System.nanoTime();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConnectionString);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        System.out.println("Try to purge Queue '" + QueueName + "'");
        AMQP.Queue.PurgeOk ok = channel.queuePurge(QueueName);
        int num = ok.getMessageCount();
        String dur = DateCalc.NanosecToString(System.nanoTime() - startAt);
        System.out.println("Purge queue (" + num + " messages) in " + dur + " sec");
    }

    private void measure(Stress.LoopBody run, String caption) throws InterruptedException {
        measure(run, caption, 3000);
    }

    private void measure(Stress.ThreadLocalInitializer init, Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        System.out.print(caption + ": ");
        Stress.Heat(run);
        List<Integer> threads = Arrays.asList(1, 2, 4, 8, 12, 16);
        ConsoleTable report = Stress.CreateReport();
        for(int t : threads)
        {
            System.out.print("{" + t + "} ");
            Stress res = Stress.Measure(TestEnv.scaleDuration(duration), 1, t, init, run);
            res.WriteToReport(report);
            Assert.assertTrue("Success counter must be above zero", res.SuccessCount > 0);
        }

        String ls = System6.lineSeparator();
        System.out.println(ls + ls + caption + ls + report);
    }

    private void measure(Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        measure(null, run, caption, duration);
    }
}
