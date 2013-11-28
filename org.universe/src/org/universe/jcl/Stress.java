package org.universe.jcl;

import org.universe.ConsoleTable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Stress {

    // If Initializer fails, Stress will rethrow exception with details
    public static interface ThreadLocalInitializer
    {
        public abstract void exec() throws Exception;
    }

    // If body fails, stress will "increment" FailCount and will continue
    public static interface LoopBody
    {
        public abstract void run() throws Exception;
    }

    private static Logger Log = Logger.getLogger(Stress.class.getName());

    public final long Count;
    public final long SuccessCount;
    public final BigDecimal Duration; // in seconds
    public final BigDecimal MilliSeconds; // msec / iter
    public final BigDecimal PerSecond; // iters / sec
    public final BigDecimal TotalDuration; // in seconds
    public final int ThreadsNumber;

    public boolean HasInitializer;
    public BigDecimal InitializerAvgDuration;
    public int NumberOfFailedInitializerThreads;

    private Stress(long count, long successCount, BigDecimal duration, BigDecimal milliSeconds, BigDecimal perSecond, BigDecimal totalDuration, int threadsNumber, BigDecimal avgInitDuration) {
        Count = count;
        SuccessCount = successCount;
        Duration = duration;
        MilliSeconds = milliSeconds;
        PerSecond = perSecond;
        TotalDuration = totalDuration;
        ThreadsNumber = threadsNumber;
        InitializerAvgDuration = avgInitDuration;
    }

    public static void Heat(final LoopBody run) throws InterruptedException
    {
        Measure(1, 1, 1, run);
        Measure(1, 1, 2, run);
    }

    public static Stress Measure(final int milliSeconds, final int stride, int numThreads, final ThreadLocalInitializer init, final LoopBody run) throws InterruptedException {
        long time0 = System.nanoTime();
        final CountDownLatch ready = new CountDownLatch(1);
        final CountDownLatch started = new CountDownLatch(numThreads);
        final AtomicLong totalCount = new AtomicLong(0);
        final AtomicLong successCount  = new AtomicLong(0);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    started.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ready.countDown();
            }
        });
        t.start();

        Iterable<Integer> range = createRange(1, numThreads, 1);
        final AtomicLong initDuration = new AtomicLong();
        Parallel.blockingFor(numThreads, range, new Parallel.Operation<Integer>() {
            @Override
            public void perform(Integer parameter) throws Exception {

                if (init != null)
                {
                    try
                    {
                        long initStartAt = System.nanoTime();
                        init.exec();
                        long initNanoSec = System.nanoTime() - initStartAt;
                        initDuration.addAndGet(initNanoSec / 1000000L);
                    }
                    catch(Exception ex)
                    {
                        started.countDown(); // bad start
                        throw new RuntimeException("Failed Stress Initialization (" + init.getClass() + ")", ex);
                    }
                }

                started.countDown(); // good start
                ready.await();

                long startAt = System.nanoTime();
                long upTo = startAt + milliSeconds * 1000000L;
                do {
                    for(int i=0; i<stride; i++)
                        try
                        {
                            run.run();
                            successCount.incrementAndGet();
                        }
                        catch(Throwable ex)
                        {
                            Log.log(Level.SEVERE, "Stress operation failed", ex);
                        }
                    totalCount.getAndAdd(stride);
                } while(System.nanoTime() < upTo);
            }
        });

        long totalDuration = System.nanoTime() - time0;
        long count = totalCount.get();
        double msecPerItem = milliSeconds / (double) count;
        double itemsPerSec = count / (double) milliSeconds * 1000d;
        t.join();
        return new Stress(count,
                successCount.get(),
                toDecimal(milliSeconds / 1000d, 0),
                count == 0 ? null : toDecimal(msecPerItem, 3),
                count == 0 ? null : toDecimal(itemsPerSec, 1),
                toDecimal(totalDuration / 1000000000d, 1),
                numThreads,
                init == null ? null : toDecimal(initDuration.get() / (double) numThreads, 0)
                );
    }

    public static Stress Measure(final int milliSeconds, final int stride, int numThreads, final LoopBody run) throws InterruptedException {
        return Measure(milliSeconds, stride, numThreads, null, run);
    }

    static BigDecimal toDecimal(double arg, int scale)
    {
        return new BigDecimal(arg).setScale(scale, RoundingMode.HALF_UP);
    }

    static Iterable<Integer> createRange(int from, int count, int delta)
    {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        int v = from;
        for(int i = 0; i<count; i++)
        {
            ret.add(new Integer(v));
            v += delta;
        }

        return ret;
    }

    public static ConsoleTable CreateReport()
    {
        ConsoleTable tbl = new ConsoleTable("#", "RUN/sec", "mSec/run", "Duration", "Total", "OK", "Fail");
        return tbl;
    }

    public void WriteToReport(ConsoleTable report)
    {
        Stress res = this;
        report  .put(res.ThreadsNumber)
                .put(res.PerSecond)
                .put(res.MilliSeconds)
                .put(res.TotalDuration)
                .put(res.Count)
                .put(res.SuccessCount == res.Count ? "100%" : String.valueOf(res.SuccessCount))
                .put(res.Count == res.SuccessCount ? "none" : String.valueOf(res.Count - res.SuccessCount))
                ;

        if (res.InitializerAvgDuration != null)
            report.put(res.InitializerAvgDuration);

        report.newRow();
    }
}
