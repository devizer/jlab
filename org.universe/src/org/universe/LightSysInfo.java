package org.universe;

import org.universe.io.SimpleStreamIterator;
import org.universe.io.WindowsSystemInfo;
import org.universe.jcl.Lazy;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*

Family: Linux, amd64
   CPU: Intel(R) Xeon(R) CPU E5-2680 v2 @ 2.80GHz, Cache 25600 KB, 2 cores
    OS: Ubuntu 13.10
  Java: OpenJDK 64-Bit Server VM, ver 1.7.0_25 by Oracle Corporation
Memory: 3,749 MB

Family: Mac OS X, x86_64
   CPU: Intel(R) Core(TM)2 Duo CPU P8400 @ 2.26GHz, Cache 3072 Kb, 2 cores
    OS: Mac OS X, 10.9
  Java: Java HotSpot(TM) 64-Bit Server VM, ver 1.7.0_45 by Oracle Corporation
Memory: 2 048 MB

Family: Linux, x86
   CPU: Intel(R) Xeon(R) CPU E5-2650 0 @ 2.00GHz, Cache 20480 KB
    OS: SUSE Linux Enterprise Server 11 (i586)
  Java: IBM J9 VM, ver 1.7.0 by IBM Corporation
Memory: 641 MB

Family: Linux, arm
   CPU: ARMv7 Processor rev 0 (v7l)
    OS: Fedora 19 (Schrödinger’s Cat)
  Java: Java HotSpot(TM) Client VM, ver 1.7.0_45 by Oracle Corporation
Memory: 1,004 MB

Family: Linux, i386
   CPU: Intel(R) Core(TM)2 Duo CPU P8400 @ 2.26GHz, Cache 3072 KB, 2 cores
    OS: Ubuntu quantal (12.10)
  Java: Java HotSpot(TM) Server VM, ver 1.7.0_45 by Oracle Corporation
Memory: 1,969 MB

        (windows without powershell 1.0+)
Family: Windows, amd64
   CPU: amd64, 2 cores
    OS: Windows 7
  Java: Java HotSpot(TM) 64-Bit Server VM, ver 1.7.0 by Oracle Corporation

Family: Windows, amd64
   CPU: Intel(R) Core(TM)2 Duo CPU P8400 @ 2.26GHz, L2 Cache 3072 KB, 2 Cores
    OS: Microsoft(R) Windows(R) XP Professional x64 Edition, Service Pack 2
  Java: Java HotSpot(TM) 64-Bit Server VM, ver 1.7.0_45 by Oracle Corporation
Memory: 511 MB
  Soft: IE 8.00.6001.18702 (longhorn_ie8_rtm(wmbla).090308-0338)
        MSI 4.5.6002.22487
        MDAC 2.82.5011.0 (srv03_sp2_qfe.120525-0350)
        Flash Player 11,4,402,287
        Excel 11.0.8342
        Word 11.0.8331
        Power Point 11.0.8335
        Outlook 11.0.8326
*/

// TODO: Beta versions on MAC OS X & Windows
public class LightSysInfo {

    public static String OS_WINDOWS = "Windows";
    public static String OS_LINUX = "Linux";
    public static String OS_MACOSX = "Mac OS X";
    public static String OS_OTHER = "Undefined";

    String osFamily;

    public LightSysInfo() {

        osFamily = getOsFamily();
    }

    public static String getOsFamily()
    {
        String raw = System.getProperty("os.name");
        if (raw != null)
        {
            String lr = raw.toLowerCase();
            if (lr.contains("windows"))
                return OS_WINDOWS;
            else if (lr.contains("linux"))
                return OS_LINUX;
            else if (lr.contains("Mac OS X".toLowerCase()))
                return OS_MACOSX;

        }
        return OS_OTHER;
    }


    public String getArch()
    {
        return System.getProperty("os.arch");
    }

/*
    public String getOsFamily() {
        return osFamily;
    }
*/

/*
java.runtime.name:Java(TM) SE Runtime Environment
java.version:1.7.0_45
java.vendor:Oracle Corporation
*/
    public String getJavaRuntimeInfo()
    {
        String name = System.getProperty("java.vm.name", "Java");
        String ver = System.getProperty("java.version");
        String vendor = System.getProperty("java.vendor");

        return String.format("%s, ver %s by %s",
                name,
                ver == null || ver.trim().length() == 0 ? "N/A" : ver.trim(),
                vendor == null || vendor.trim().length() == 0 ? "Unknown vendor" : vendor.trim()
        );
    }

    public String getHumanReadableString()
    {
        String ls = System6.lineSeparator();
        StringBuilder ret = new StringBuilder();
        ret.append("Family: " + getOsFamily() + ", " + this.getArch()).append(ls);
        ret.append("   CPU: " + this.getProcessorName()).append(ls);
        ret.append("    OS: " + this.getOsName()).append(ls);
        ret.append("  Java: " + this.getJavaRuntimeInfo()).append(ls);
        Integer mem = getMemMegabytes();
        if (mem != null)
            ret.append("Memory: " + new DecimalFormat("###,###,### 'MB'").format(mem.intValue())).append(ls);

        if (OS_WINDOWS.equals(osFamily))
            writeWindowsDetails(ret);

        return ret.toString();
    }

    private void writeWindowsDetails(StringBuilder ret) {

        List<String> list = Arrays.asList(
                "IE", WindowsSystemInfo.ID.FileVersion_IE,
                "MSI", WindowsSystemInfo.ID.FileVersion_MSI,
                "MDAC", WindowsSystemInfo.ID.FileVersion_MDAC,
                "Flash Player", WindowsSystemInfo.ID.FileVersion_FlashPlayer,
                "Excel", WindowsSystemInfo.ID.FileVersion_Excel,
                "Word", WindowsSystemInfo.ID.FileVersion_Word,
                "Power Point", WindowsSystemInfo.ID.FileVersion_PowerPoint,
                "Outlook", WindowsSystemInfo.ID.FileVersion_Outlook);

        Map<String, String> map = null;
        try {
            map = this.WinSysInfo.get().asMap();
        } catch (Exception ex) {
            // no additional info
            return;
        }

        for (int i = 0; i < list.size(); i += 2) {
            String title = list.get(i);
            String key = list.get(i + 1);
            String ver = map.get(key);
            if (ver != null)
                ret     .append(i == 0 ? "  Soft: " : "        ")
                        .append(String.format("%s %s", title, ver))
                        .append(System6.lineSeparator());
        }
    }

    // Nullable
    public Integer getMemMegabytes()
    {
        if (OS_LINUX.equals(osFamily))
            return getMemMegabytesOnLinux();
        else if (OS_MACOSX.equals(osFamily))
            return getMemMegabytesOnMac();
        else if (OS_WINDOWS.equals(osFamily))
        {
            try { return WinSysInfo.get().getAvailableRamInMB(); }
            catch(Exception ex){}
        }

        return null;
    }

    // MemTotal:        1028116 kB
    // will return null, if unknown
    private Integer getMemMegabytesOnLinux() {
        String raw = null;
        SimpleStreamIterator iter = null;
        try
        {
            iter = new SimpleStreamIterator(new File("/proc/meminfo"), new SimpleStreamIterator.Options(":", true, true, true));
            SimpleStreamIterator.Row row;
            while((row = iter.next()) != null)
            {
                if ("memtotal".equals(row.Key))
                    raw = row.Value;
            }
        }
        finally
        {
            if (iter != null) iter.close();
        }

        int ret = -1;
        if (raw != null)
        {
            if (raw.endsWith("kb"));
            try
            {
                ret = Integer.parseInt(raw.substring(0, raw.length()-2).trim()) / 1024;
            }
            catch(NumberFormatException ex){}
        }

        return ret > 0 ? new Integer(ret) : null;
    }

    public String getOsName()
    {
        if (OS_LINUX.equals(osFamily))
            return getLinuxPrettyOsName();

        if (OS_MACOSX.equals(osFamily))
        {
            String v1 = System.getProperty("os.name");
            String v2 = System.getProperty("os.version");
            return v2 == null || v2.length() == 0
                    ? v1
                    : v1 + ", " + v2;
        }

        if (OS_WINDOWS.equals(osFamily))
        {
            try { return WinSysInfo.get().getOsName(); }
            catch(Exception ex) {}
        }

        return System.getProperty("os.name");
    }

    private String getLinuxPrettyOsName()  {
        // etc/*release
        File etc = new File("/etc");
        File[] files = etc.listFiles();
        String firstLine = null; // SUSE
        for(File f : files)
        {
            if (f.isFile() && f.getName().toLowerCase().endsWith("release"))
            {
                SimpleStreamIterator iter = null;
                try
                {
                    iter = new SimpleStreamIterator(
                            new FileInputStream(f.getAbsolutePath()),
                            new SimpleStreamIterator.Options("=", true, true, true));

                    SimpleStreamIterator.Row row = null;
                    while((row = iter.next()) != null)
                    {
                        if ("pretty_name".equals(row.Key))
                        {
                            String prettyName = row.Value;
                            if (prettyName.length() > 0)
                                return trimQuotes(prettyName);
                        }

                        // SUSE, no PRETTY_NAME
                        String line = row.Line;
                        if (line.length() > 0 && line.indexOf('=') == -1 && firstLine == null)
                            firstLine = line;
                    }
                }
                catch (FileNotFoundException ex) {
                    throw new RuntimeException("Unable to read os info from '" + f.getAbsolutePath() + "'", ex);
                }
                finally
                {
                    if (iter != null) iter.close();
                }
            }
        }

        return firstLine == null ? System.getProperty("os.name") : firstLine;
    }


    public String getProcessorName()
    {
        String ret = getArch();
        if (OS_LINUX.equals(osFamily))
            ret = getLinuxProcessorName();

        else if (OS_MACOSX.equals(osFamily))
        {
            String name = getMacProcessorName();
            ret = name == null ? getArch() : name;
        }

        else if (OS_WINDOWS.equals(osFamily))
        {
            try { ret = WinSysInfo.get().getProcessorName(); }
            catch(Exception ex) {}
        }

        int procs = Runtime.getRuntime().availableProcessors();
        if (procs > 1)
            ret = ret.length() == 0 ? ("Cores: " + procs) : (ret + ", " + procs + " cores");

        return ret;
    }

    private String getLinuxProcessorName() {
        String name = null, cache = null;
        SimpleStreamIterator iter = null;
        try
        {
            iter = new SimpleStreamIterator(new File("/proc/cpuinfo"), new SimpleStreamIterator.Options(":", true, true, true));
            SimpleStreamIterator.Row row;
            while((row = iter.next()) != null)
            {
                if ("model name".equals(row.Key))
                    name = row.Value;
                else if ("cache size".equals(row.Key))
                    cache = row.Value;
            }

            name = name == null || name.length() == 0 ? getArch() : removeContinuousSpaces(name);
            cache = cache == null ? "" : cache;
        }
        finally
        {
            if (iter != null) iter.close();
        }

        return name + (cache.length() == 0 ? "" : (", Cache " + cache));
    }

    static String removeContinuousSpaces(String arg)
    {
        arg = arg.replace('\t', ' ');
        while(arg.indexOf("  ") >= 0)
            arg = arg.replace("  ", " ");

        return arg;
    }

    static String trimQuotes(String arg)
    {
        if (arg.charAt(0) == '"' && arg.charAt(arg.length()-1) == '"')
        {
            return arg.length() > 2 ? arg.substring(1, arg.length()-1) : "";
        }

        return arg;
    }

    // returns null if `sysctl machdep.cpu` fails, or [machdep.cpu.brand_string] undefined
    static String getMacProcessorName()
    {
        Ref<String> name = new Ref<String>();
        Ref<Integer> cache = new Ref<Integer>();

        try {
            if (getMacProcessorName(name, cache))
                return name.Value + ", Cache " + cache.Value + " Kb";
        } catch (IOException e) {
        }

        // cache is unknown, name is ok
        if (name.Value != null)
            return name.Value;

        // unknown
        return null;
    }

    private Integer getMemMegabytesOnMac()  {

        int ret = -1;
        try
        {
            Process process = Runtime.getRuntime().exec("sysctl -n hw.memsize");
            InputStream stdout = process.getInputStream();

            SimpleStreamIterator iter = new SimpleStreamIterator(stdout, new SimpleStreamIterator.Options(":", true, true, true));
            SimpleStreamIterator.Row row;
            try
            {
                while((row = iter.next()) != null)
                {
                    // System.out.println(row.Key + ": " + row.Value);
                    if (row.Line.length() > 0)
                    {
                        long bytes;
                        long m1 = 1024*1024L;
                        try
                        {
                            if ((bytes = Long.parseLong(row.Line)) >= m1)
                                return new Integer((int) (bytes / m1));
                        }
                        catch(NumberFormatException ex) {}
                    }
                }
            }
            finally
            {
                if (iter != null) iter.close();
            }
        }
        catch(IOException ex)
        {
            return null;
        }

        return ret > 0 ? new Integer(ret) : null;
    }


    // Non thread safe
    static boolean getMacProcessorName(Ref<String> name, Ref<Integer> cache) throws IOException {
        name.Value = null;
        cache.Value = null;

        Process process = Runtime.getRuntime().exec("sysctl machdep.cpu");
        InputStream stdout = process.getInputStream();

        SimpleStreamIterator iter = new SimpleStreamIterator(stdout, new SimpleStreamIterator.Options(":", true, true, true));
        SimpleStreamIterator.Row row;
        while((row = iter.next()) != null)
        {
            if ("machdep.cpu.brand_string".equals(row.Key))
            {
                String rawName = row.Value;
                if (rawName != null || rawName.length() > 0)
                    name.Value = removeContinuousSpaces(rawName);
            }

            if ("machdep.cpu.cache.size".equals(row.Key))
            {
                try { cache.Value = Integer.parseInt(row.Value); }
                catch(NumberFormatException ex){}
            }

        }

        return name.Value != null && name.Value.length() > 0
                && cache.Value != null && cache.Value > 0;
    }

    // if powershell is absent, it will returns null on each call
    private Lazy<WindowsSystemInfo> WinSysInfo = new Lazy<WindowsSystemInfo>(Lazy.Mode.ExecutionAndPublication) {
        @Override
        protected WindowsSystemInfo initialValue() throws Exception {
            if (!OS_WINDOWS.equals(osFamily))
                throw new IllegalStateException("Windows System Info requires Windows");

            try { return WindowsSystemInfo.get(); }
            catch(Exception ex) { return null; }
        }
    };

    /*
kern.nx: 1
kern.osversion: 13A598
kern.safeboot: 0
*/


}
