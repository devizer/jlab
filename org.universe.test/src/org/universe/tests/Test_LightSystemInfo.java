package org.universe.tests;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.universe.ConsoleTable;
import org.universe.LightSysInfo;
import org.universe.System6;
import org.universe.jcl.Stress;
import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Test_LightSystemInfo {

    @Test
    public void Test()
    {
        LightSysInfo info = new LightSysInfo();

        Assert.assertTrue("Family is known", info.getOsFamily().length() > 0);
        Assert.assertTrue("CPU is known", info.getProcessorName().length() > 0);
        Assert.assertTrue("OS Pretty Name is known", info.getOsName().length() > 0);
        Assert.assertTrue("Java Runtime is pretty", info.getJavaRuntimeInfo().length() > 0);

        System.out.print(info.getHumanReadableString());
    }

    @Test
    public void Z_Measure_LightSystemInfo() throws Exception {

        Stress.LoopBody iteration = new Stress.LoopBody() {
            @Override
            public void run()  {
                new LightSysInfo().getHumanReadableString();
            }
        };

        measure(iteration, "Measure LightSystemInfo", 2000);
    }


    private void measure(Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        System.out.print(caption + ": ");
        Stress.Heat(run);
        List<Integer> threads = Arrays.asList(1, 2, 6);
        ConsoleTable tbl = Stress.CreateReport();
        for(int t : threads)
        {
            System.out.print(t + " ");
            Stress res = Stress.Measure(TestEnv.scaleDuration(duration), 1, t, run);
            res.WriteToReport(tbl);
        }

        System.out.println(System6.lineSeparator() + System6.lineSeparator() + caption + System6.lineSeparator() + tbl);
    }


}
