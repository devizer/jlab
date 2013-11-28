package org.universe.io;

import org.universe.DateCalc;
import org.universe.jcl.Lazy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class WindowsSystemInfo {

    private Map<String, String> Strings = new HashMap<String, String>();

    public static class Version
    {
        public Integer Major;
        public Integer Minor;

        public Version clone()
        {
            Version ret = new Version();
            ret.Major = this.Major;
            ret.Minor = this.Minor;
            return ret;
        }

        @Override
        public String toString() {
            return Major + "." + Minor;
        }
    }

    public String getProcessorName()
    {
        Integer l2 = tryInteger(ID.Processor_L2CacheSize);
        Integer l3 = tryInteger(ID.Processor_L3CacheSize);
        String proc = Strings.get(ID.Processor_Name);

        return join(Arrays.asList(
                proc == null ? null : removeContinuousSpaces(proc),
                l2 != null && l2 > 0 ? "L2 Cache " + l2 + " KB" : null,
                l3 != null && l3 > 0 ? "L3 Cache " + l3 + " KB" : null));
    }

    public Integer getAvailableRamInMB()
    {
        Integer ram = tryInteger(ID.OperatingSystem_TotalVisibleMemorySize);
        return ram != null && ram > 0 ? ram / 1024 : null;
    }

    public String getOsName()
    {
        Integer sp = tryInteger(ID.OperatingSystem_ServicePackMajorVersion);
        String os = Strings.get(ID.OperatingSystem_Caption);
        String arch = Strings.get(ID.OperatingSystem_OSArchitecture);

        return join(Arrays.asList(
                os,
                sp != null && sp > 0 ? "Service Pack " + sp : null,
                arch));
    }

    public Version getWindowsVersion()
    {
        // 6.1.7601
        String raw = Strings.get(ID.OperatingSystem_Version);
        if (raw != null)
        {
            String[] arr = raw.split("\\.");
            if (arr != null && arr.length >= 2)
            {
                try
                {
                    Version ret = new Version();
                    ret.Major = Integer.parseInt(arr[0]);
                    ret.Minor = Integer.parseInt(arr[1]);
                    return ret;
                }
                catch(NumberFormatException ex)
                {
                    return null;
                }
            }
        }
        return null;
    }

    public Integer getServicePack()
    {
        return tryInteger(ID.OperatingSystem_ServicePackMajorVersion);
    }

    public String getSystemDrive()
    {
        return Strings.get(ID.OperatingSystem_SystemDrive);
    }

    public String getSystemDirectory()
    {
        return Strings.get(ID.OperatingSystem_SystemDirectory);
    }

    public Map<String,String> asMap()
    {
        return new HashMap<String, String>(Strings);
    }

    String join(List<String> list)
    {
        StringBuilder ret = new StringBuilder();
        for(String x : list)
            if (x != null && x.length() > 0)
                ret.append(ret.length() == 0 ? "" : ", ").append(x);

        return ret.toString();
    }

    Integer tryInteger(String id)
    {
        try
        {
            return Integer.parseInt(Strings.get(id));
        }
        catch(Exception ex) { return null; }
    }


    public static class ID
    {
        public static final String
                Processor_Name                          = "Processor.Name"                         , // 'Intel(R) Core(TM)2 Duo CPU P8400 @ 2.26GHz'
                Processor_L2CacheSize                   = "Processor.L2CacheSize"                  , // '3072'
                Processor_L3CacheSize                   = "Processor.L3CacheSize"                  , // '0'
                Processor_MaxClockSpeed                 = "Processor.MaxClockSpeed"                , // '2268'
                ComputerSystem_Manufacturer             = "ComputerSystem.Manufacturer"            , // 'ASUSTeK Computer Inc.'
                ComputerSystem_Model                    = "ComputerSystem.Model"                   , // 'M50Vn'
                ComputerSystem_TotalPhysicalMemory      = "ComputerSystem.TotalPhysicalMemory"     , // '8589004800'
                OperatingSystem_Caption                 = "OperatingSystem.Caption"                , // 'Microsoft Windows 7 Enterprise'
                OperatingSystem_Version                 = "OperatingSystem.Version"                , // '6.1.7601'
                OperatingSystem_ServicePackMajorVersion = "OperatingSystem.ServicePackMajorVersion", // '1'
                OperatingSystem_ServicePackMinorVersion = "OperatingSystem.ServicePackMinorVersion", // '0'
                OperatingSystem_OSArchitecture          = "OperatingSystem.OSArchitecture"         , // '64-bit'
                OperatingSystem_TotalVirtualMemorySize  = "OperatingSystem.TotalVirtualMemorySize" , // '16773540'
                OperatingSystem_TotalVisibleMemorySize  = "OperatingSystem.TotalVisibleMemorySize" , // '8387700'
                OperatingSystem_FreePhysicalMemory      = "OperatingSystem.FreePhysicalMemory"     , // '1172492'
                OperatingSystem_FreeVirtualMemory       = "OperatingSystem.FreeVirtualMemory"      , // '6853556'
                OperatingSystem_FreeSpaceInPagingFiles  = "OperatingSystem.FreeSpaceInPagingFiles" , // '6116896'
                OperatingSystem_SystemDrive             = "OperatingSystem.SystemDrive"            , // 'C:'
                OperatingSystem_SystemDirectory         = "OperatingSystem.SystemDirectory"        , // 'C:\Windows\system32'

                FileVersion_MDAC         = "VersionInfo.MDAC.FileVersion"          , // '6.1.7601.17857 (win7sp1_gdr.120605-1503)'
                FileVersion_MSI          = "VersionInfo.MSI.FileVersion"           , // '5.0.7601.17807'
                FileVersion_IE           = "VersionInfo.IE.FileVersion"            , // '10.00.9200.16521 (win8_gdr_soc_ie.130216-2100)'
                FileVersion_FlashPlayer  = "VersionInfo.FlashPlayer.FileVersion"   , // '11,7,700,224'
                FileVersion_Excel        = "VersionInfo.Excel.FileVersion"         , // '12.0.6665.5003'
                FileVersion_Word         = "VersionInfo.Word.FileVersion"          , // '12.0.6668.5000'
                FileVersion_PowerPoint   = "VersionInfo.PowerPoint.FileVersion"    , // '12.0.6600.1000'
                FileVersion_Outlook      = "VersionInfo.Outlook.FileVersion"       ; // '12.0.6600.1234'

        static final List<String> All = Arrays.asList(
                Processor_Name                          ,
                Processor_L2CacheSize                   ,
                Processor_L3CacheSize                   ,
                Processor_MaxClockSpeed                 ,
                ComputerSystem_Manufacturer             ,
                ComputerSystem_Model                    ,
                ComputerSystem_TotalPhysicalMemory      ,
                OperatingSystem_Caption                 ,
                OperatingSystem_Version                 ,
                OperatingSystem_ServicePackMajorVersion ,
                OperatingSystem_ServicePackMinorVersion ,
                OperatingSystem_OSArchitecture          ,
                OperatingSystem_TotalVirtualMemorySize  ,
                OperatingSystem_TotalVisibleMemorySize  ,
                OperatingSystem_FreePhysicalMemory      ,
                OperatingSystem_FreeVirtualMemory       ,
                OperatingSystem_FreeSpaceInPagingFiles  ,
                OperatingSystem_SystemDrive             ,
                OperatingSystem_SystemDirectory         ,

                FileVersion_MSI           ,
                FileVersion_IE            ,
                FileVersion_MDAC          ,
                FileVersion_FlashPlayer   ,
                FileVersion_Excel         ,
                FileVersion_Word          ,
                FileVersion_PowerPoint    ,
                FileVersion_Outlook
        );
    }

    public static WindowsSystemInfo get()
    {
        try { return Builder.get().clone(); }
        catch (Exception ex) {
            throw new RuntimeException("Failed to collect Windows System Information", ex);
        }
    }

    protected WindowsSystemInfo clone()
    {
        WindowsSystemInfo ret = new WindowsSystemInfo();
        ret.Strings = new HashMap<String, String>(Strings);
        return ret;
    }

    public WindowsSystemInfo() {
    }


    final static Lazy<WindowsSystemInfo> Builder = new Lazy<WindowsSystemInfo>(Lazy.Mode.ExecutionAndPublication)
    {
        @Override
        protected WindowsSystemInfo initialValue() throws Exception {
            return Build();
        }
    };

    private static WindowsSystemInfo Build() throws IOException {

        long startAt = System.nanoTime();
        Process process = Runtime.getRuntime().exec(InternalWindowsInfoScript.Shell);

        // input stream
        OutputStreamWriter wr = new OutputStreamWriter(process.getOutputStream(), "utf-8");
        wr.write(InternalWindowsInfoScript.PS1);
        wr.close();

        // result
        InputStream stdout = process.getInputStream();
        SimpleStreamIterator iter = null;
        WindowsSystemInfo ret = new WindowsSystemInfo();
        ret.Strings = new HashMap<String, String>();
        try
        {
            iter = new SimpleStreamIterator(stdout, new SimpleStreamIterator.Options(":", true, true, false));

            SimpleStreamIterator.Row row;
            String group = "";
            while((row = iter.next()) != null)
            {
                if (row.Line.length() > 0)
                {
                    if ("group-header".equals(row.Key))
                        group = row.Value;

                    else if (row.Key != null)
                    {
                        String key = row.Key;
                        String rawValue = row.Value;
                        String s1 = "_" + group + "_" + key;
                        String fullKey = "" + group + "." + key;
                        // System.out.println(String.format("%-40s = %-40s, // '%s'", s1,'"' + fullKey + '"',rawValue));
                        if (ID.All.contains(fullKey))
                        {
                            ret.Strings.put(fullKey, rawValue);
                            // System.out.println(fullKey + " = " + rawValue);
                        }
                    }
                }
            }
        }
        finally
        {
            if (iter != null) iter.close();
        }

        String footprint = String.format(
                "Windows System Info footprint: %s secs",
                DateCalc.NanosecToString(System.nanoTime() - startAt));

        System.out.println(footprint);

        return ret;
    }

    static String removeContinuousSpaces(String arg)
    {
        arg = arg.replace('\t', ' ');
        while(arg.indexOf("  ") >= 0)
            arg = arg.replace("  ", " ");

        return arg;
    }

}
