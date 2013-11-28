package org.universe.rabbitstress;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static String QUEUE_NAME = "Stress-Queue";
    private static CountDownLatch Started = new CountDownLatch(1);
    private static final int NUM_CONSUMERS = 3;
    private static int NUM_PRODUCERS = 2;

    public static void main(String[] args) throws Exception {


        for(int i=1; i<=NUM_CONSUMERS; i++)
            RunConsumer(i);

        for(int i=1; i<=NUM_PRODUCERS; i++)
            RunProducer(i);
    }

    static void RunConsumer(final int number)  {
        RunRunnable("Consumer-" + number, new Runnable() {
            @Override
            public void run() {
                try {
                    Started.await();
                    String pre = String.format("[Con %d] ", number);

                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost("localhost");
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    boolean autoAck = false;
                    channel.basicConsume(QUEUE_NAME, autoAck, "The Consumer", consumer);
                    System.out.println(pre + "Consumer is running!");

                    while (true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = pre + new String(delivery.getBody());

                        if (Math.random() > 0.1)
                        {
                            // ack only one
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                            System.out.println(message + ": Handled");
                        }
                        else
                        {
                            boolean needRequeue = Math.random() > 0.5;
                            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), needRequeue);
                            System.out.println(message + ": " + (needRequeue ? "Requeued" : "Dropped"));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    static void RunProducer(final int number) throws IOException, InterruptedException {
        RunRunnable("Producer-" + number, new Runnable() {
            @Override
            public void run() {
                try {

                    final String pre = String.format("[Producer %d] ", number);
                    System.out.println(pre + "Started");
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost("localhost");
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();
                    AtomicInteger count = new AtomicInteger(1);

                    boolean isDurable = true;
                    boolean isExclusive = false;
                    boolean isAutoDelete = false;
                    channel.queueDeclare(QUEUE_NAME, isDurable, isExclusive, isAutoDelete, null);
                    System.out.println(pre + "Queue is declared");
                    Started.countDown();
                    while (true) {
                        Thread.sleep(1);
                        String s = "Hello World! " + count.getAndIncrement();
                        channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, s.getBytes());
                        System.out.println(pre + "Sent '" + s + "'");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void RunRunnable(String name, Runnable action)
    {
        Thread t = new Thread(action);
        t.setDaemon(false);
        t.setName(name);
        t.start();
    }
}
