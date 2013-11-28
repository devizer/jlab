package org.universe.queue.test;

import org.universe.DateCalc;
import org.universe.queue.Message;
import org.universe.queue.SimpleQueue;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/24/13
 * Time: 4:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class MillionsMessages {

    final static Object UniqueProcessId = UUID.randomUUID().toString();
    private SimpleQueue queue;
    Random rnd = new Random(1);
    static AtomicInteger counter = new AtomicInteger();
    private String queueName;

    public static void main(String... args) throws Exception {
        MillionsMessages mm = new MillionsMessages();
        mm.Publish_Millions();
    }

    public void Publish_Millions() throws Exception {

        queue = new SimpleQueue(EnvQueue.createDataSource());
        queueName = "Dev-Stress-Lab";

        int n = 10000000;
        long nanoPublish = System.nanoTime();
        long buffer = 0;
        for(int i=0; i< n; i++)
        {
            String key = UUID.randomUUID().toString();
            byte[] message = new byte[0];
            rnd.nextBytes(message);
            buffer++;
            // key = null;
            queue.publish(queueName, key, message);
            if (buffer == 100)
            {
                long nsec = System.nanoTime() - nanoPublish;

                long nsecPerDelivery;
                long nsecPerAck;
                {
                    long n1 = System.nanoTime();
                    Message msg = queue.nextDelivery(queueName);
                    assert msg != null;
                    nsecPerDelivery = System.nanoTime() - n1;
                    n1 = System.nanoTime();
                    queue.ack(queueName, msg.getId());
                    nsecPerAck = System.nanoTime() - n1;

                    // queue.dropMessageFromStorage(msg.getId());
                }

                System.out.println(SimpleQueue.LocalStat.toConsoleTable());
                System.out.println(String.format(
                        "%d, %s msec per 1000 msgs, %s per 1 delivery, %s per 1 ack",
                        i,
                        DateCalc.NanosecToString(nsec * 10),
                        DateCalc.NanosecToString(nsecPerDelivery, 4),
                        DateCalc.NanosecToString(nsecPerAck, 4)
                ));

                buffer = 0;
                nanoPublish = System.nanoTime();
            }
        }

    }

}
