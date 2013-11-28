package org.universe.queue;

import org.universe.ConsoleTable;

import java.util.*;

// статистика не защищена от misuse
public class QueueStatistic {

    Counters total = new Counters();
    Map<String, Counters> byQueue = new HashMap<String, Counters>();
    Object sync = new Object();

    public class Counters
    {
        public long Publish;
        public long Postpone;
        public long Ack;
        public long InProcess;

        public Counters() {
        }

        public Counters(long publish, long postpone, long ack, long inProcess) {
            Publish = publish;
            Postpone = postpone;
            Ack = ack;
            InProcess = inProcess;
        }

        public Counters clone()
        {
            return new Counters(Publish, Postpone, Ack, InProcess);
        }
    }

    public Counters getTotal() {
        synchronized (sync)
        {
            return total.clone();
        }
    }

    public Counters getByQueue(String queueName)
    {
        synchronized (sync)
        {
            Counters ret = byQueue.get(queueName);
            return ret == null ? new Counters() : ret.clone();
        }
    }

    public QueueStatistic clone()
    {
        synchronized (sync)
        {
            QueueStatistic ret = new QueueStatistic();
            ret.total = total.clone();
            for(Map.Entry<String,Counters> e : byQueue.entrySet())
                ret.byQueue.put(e.getKey(), e.getValue().clone());

            return ret;
        }
    }

    void incAck(String queueName)
    {
        synchronized (sync)
        {
            total.Ack++;
            getOrCreate(queueName).Ack++;
        }
    }

    void incPublish(String queueName)
    {
        synchronized (sync)
        {
            total.Publish++;
            getOrCreate(queueName).Publish++;
        }
    }

    void incPostpone(String queueName)
    {
        synchronized (sync)
        {
            total.Postpone++;
            getOrCreate(queueName).Postpone++;
        }
    }

    void incInProcess(String queueName, int delta)
    {
        synchronized (sync)
        {
            total.InProcess = total.InProcess + delta;
            Counters counters = getOrCreate(queueName);
            counters.InProcess = counters.InProcess + delta;
        }
    }

    Counters getOrCreate(String queueName)
    {
        Counters ret = byQueue.get(queueName);
        if (ret == null)
        {
            ret = new Counters();
            byQueue.put(queueName, ret);
        }

        return ret;
    }

    public ConsoleTable toConsoleTable()
    {
        ConsoleTable ret = new ConsoleTable("Publish", "Ack", "InProcess", "Postpone", "Queue");
        final Comparator<String> c = String.CASE_INSENSITIVE_ORDER;
        synchronized (sync)
        {
            ArrayList<Map.Entry<String, Counters>> sorted = new ArrayList<Map.Entry<String, Counters>>(byQueue.entrySet());
            Collections.sort(sorted, new Comparator<Map.Entry<String, Counters>>() {
                @Override
                public int compare(Map.Entry<String, Counters> o1, Map.Entry<String, Counters> o2) {
                    return c.compare(o1.getKey(), o2.getKey());
                }
            });

            writeToTable(ret, "TOTAL", this.total);
            for(Map.Entry<String, Counters> x : sorted)
                writeToTable(ret, x.getKey(), x.getValue());
        }

        return ret;
    }

    static void writeToTable(ConsoleTable tbl, String name, Counters counters)
    {
        tbl     .put(counters.Publish)
                .put(counters.Ack)
                .put(counters.InProcess)
                .put(counters.Postpone)
                .put(name)
                .newRow();
    }



}
