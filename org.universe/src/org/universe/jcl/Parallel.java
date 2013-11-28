package org.universe.jcl;

/*
* Copyright 2013 Vladimir Goyda. Changes:
* 1. elements arg supports null-elements
* 2. exception never forgotten. Blocking call will return AggregateException,
*    when at leeast 1 operation throws exception
*
* Copyright 2011 Matt Crinklaw-Vogt
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

import java.util.*;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parallel {

    static final int NUM_PROC = Runtime.getRuntime().availableProcessors();
    private static final int NUM_CORES = 1024;
    // static final Logger log = Logger.getLogger(Parallel.class.getName());


    public static <T> void blockingFor(
            final Iterable<? extends T> elements,
            final Operation<T> operation) {
        blockingFor(2 * NUM_PROC, elements, operation);
    }

    public static <T> void blockingFor(
            int numThreads,
            final Iterable<? extends T> elements,
            final Operation<T> operation)
    {
        For(numThreads, new NamedThreadFactory("Parallel.For", true), elements, operation,
                Integer.MAX_VALUE, TimeUnit.DAYS);
    }

    public static <T> void For(
            final Iterable<? extends T> elements,
            final Operation<T> operation) {
        For(2 * NUM_CORES, elements, operation);
    }

    public static <T> void For(
            int numThreads,
            final Iterable<? extends T> elements,
            final Operation<T> operation)
    {
        For(numThreads, new NamedThreadFactory("Parallel.For", true),
                elements, operation, null, null);
    }

    public static <S extends T, T> void For(
            int numThreads,
            NamedThreadFactory threadFactory,
            final Iterable<S> elements,
            final Operation<T> operation,
            Integer wait,
            TimeUnit waitUnit) {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        final ThreadSafeIterator<S> itr = new ThreadSafeIterator<S>(elements.iterator());
        final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<Exception>());
        final AtomicBoolean hasException = new AtomicBoolean(false);

        for (int i = 0; i < threadPoolExecutor.getMaximumPoolSize(); i++){
            threadPoolExecutor.submit(new Callable<Void>() {
                @Override
                public Void call() {
                    Next next;
                    while ((next = itr.next()) != null && !hasException.get()) {
                        try {
                            operation.perform((T) next.value);
                        } catch (Exception e) {
                            exceptions.add(e);
                            hasException.set(true);
                            // log.log(Level.SEVERE, "Exception during execution of parallel task", e);
                        }
                    }
                    return null;
                }
            });
        }

        threadPoolExecutor.shutdown();

        if (wait != null) {
            try {
                threadPoolExecutor.awaitTermination(wait, waitUnit);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        if (exceptions.size() > 0)
            throw new AggregateException(exceptions);

    }

    // Iterable supports null "list" items
    static class Next
    {
        Object value;

        Next(Object value) {
            this.value = value;
        }
    }

    private static class ThreadSafeIterator<T> {

        private final Iterator<T> itr;

        public ThreadSafeIterator(Iterator<T> itr) {
            this.itr = itr;
        }

        public synchronized Next next() {
            if (itr.hasNext())
                return new Next(itr.next());
            else
                return null;
        }

    }


    public static <T> Collection<Callable<Void>> createCallables(final Iterable<T> elements, final Operation<T> operation) {
        List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
        for (final T elem : elements) {
            callables.add(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                        operation.perform(elem);
                        return null;
                }
            });
        }

        return callables;
    }

    public static interface Operation<T>  {
        public void perform(T parameter) throws Exception;
    }

}

