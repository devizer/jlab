package org.universe.queue.test;

import org.universe.queue.SimpleQueue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

// 1 раз в сутки, в Hour_Of_Day:00 чистит старые сообщения
public class Purge_Smelling_Message_Sample {

    final SimpleQueue Queue = null;
    final String QueueName = "Sample Queue";
    final int Hour_Of_Day = 3;
    final int SmellingThreshold = 10 * 24 * 3600;

    public void start()
    {
        final String threadName = "Purge smelling message of queue '" + QueueName + "'";
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String prev = null;
                while(true)
                {
                    try {
                        Thread.sleep(60*5*1000);
                        Calendar c = Calendar.getInstance();
                        Date now = new Date();
                        c.setTime(now);
                        if (c.get(Calendar.HOUR_OF_DAY) == Hour_Of_Day)
                        {
                            String key = format.format(now);
                            if (prev == null || !key.equals(prev))
                            {
                                prev = key;
                                Queue.purgeOldMessages(QueueName, SmellingThreshold);
                            }
                        }

                    }
                    catch(Exception ex)
                    {
                        Logger
                                .getLogger("Purge_smelling_messages")
                                .log(Level.WARNING, threadName + " skiipped", ex);
                    }
                }
            }
        });

        t.setDaemon(true);
        t.setName(threadName);
        t.start();
    }
}
