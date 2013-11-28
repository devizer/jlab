package org.universe.jcl;

import java.util.Collection;
import java.util.LinkedList;

public class ParallelSample
{
    public static void Run()
    {
        // Collection of items to process in parallel
        Collection<Integer> elems = new LinkedList<Integer>();
        for (int i = 0; i < 40; ++i) {
            elems.add(i);
        }
        Parallel.blockingFor(elems,
                // The operation to perform with each item
                new Parallel.Operation<Integer>() {
                    public void perform(Integer param) {

                        System.out.println("S" + param);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        System.out.println("E" + param);

                    }

                    ;
                });
    }
}
