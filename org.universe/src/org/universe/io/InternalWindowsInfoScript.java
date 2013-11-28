package org.universe.io;

import org.universe.System6;

class InternalWindowsInfoScript {

    // compliant with 'All Signed' execution policy
    final static String Shell = "powershell -OutputFormat Text -Command -";

    static final String PS1;

    final static String PS1_Light = "" +
            "Write-Host 'group-header: Processor'\n" +
            "Get-WmiObject -Class Win32_Processor | Format-List Name,L2CacheSize,L3CacheSize,MaxClockSpeed\n" +
            "\n" +
            "Write-Host 'group-header: ComputerSystem'\n" +
            "Get-WmiObject -Class Win32_ComputerSystem | Format-List Manufacturer,Model,TotalPhysicalMemory\n" +
            "\n" +
            "Write-Host 'group-header: OperatingSystem'\n" +
            "Get-WmiObject -Class Win32_OperatingSystem | Format-List Caption,Version,ServicePackMajorVersion,ServicePackMinorVersion,OSArchitecture,TotalVirtualMemorySize,TotalVisibleMemorySize,FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles,SystemDrive,SystemDirectory\n";

    final static String PS1_Hard = "" +
            "Function GetComImplementation\n" +
            "{\n" +
            " Param([string] $key, [string] $progId);\n" +
            " $r1 = Get-ItemProperty -Path Registry::HKEY_CLASSES_ROOT\\$progId\\CLSID -Name '' -ErrorAction SilentlyContinue;\n" +
            " $guid = $r1.'(default)';\n" +
            " if ($guid) \n" +
            " {\n" +
            "  Write-Host \"GUID of\" $progId \"is\" $guid;\n" +
            "  $suffixes = @(\"CLSID\\$guid\\InprocServer32\", \n" +
            "                \"CLSID\\$guid\\LocalServer32\", \n" +
            "                \"Wow6432Node\\CLSID\\$guid\\InprocServer32\", \n" +
            "                \"Wow6432Node\\CLSID\\$guid\\LocalServer32\");\n" +
            "\n" +
            "     $done = $false;\n" +
            "     foreach($suf in $suffixes)\n" +
            "     {\n" +
            "      $r2 = Get-ItemProperty -Path Registry::HKEY_CLASSES_ROOT\\$suf -Name '' -ErrorAction SilentlyContinue;\n" +
            "      if (($r2 -ne $null) -and (-not $done))\n" +
            "      {\n" +
            "       $try1 = $r2.'(default)'.ToString();\n" +
            "       # Write-Host 'Candidate of ' $progId ' is '$try1' ' \n" +
            "       if ( $try1.StartsWith('\"') -eq \"True\" )\n" +
            "       {\n" +
            "          $index = $try1.IndexOf('\"', 1);\n" +
            "          if ($index -ge 2)\n" +
            "          {\n" +
            "            $try1 = $try1.Substring(1, $index - 1);\n" +
            "            # Write-Host 'Fixed candidate is ' $try1;\n" +
            "          }\n" +
            "       }\n" +
            "\n" +
            "       if ( -not [System.IO.File]::Exists($try1) )\n" +
            "       {\n" +
            "          $index = $try1.IndexOf(' ', 0);\n" +
            "          if ($index -ge 1)\n" +
            "          {\n" +
            "            $try1 = $try1.Substring(0, $index);\n" +
            "          }\n" +
            "       }\n" +
            "\n" +
            "       if ( [System.IO.File]::Exists($try1) )\n" +
            "       {\n" +
            "         $ver = [System.Diagnostics.FileVersionInfo]::GetVersionInfo($try1)\n" +
            "         if ($ver)\n" +
            "         {\n" +
            "           Write-Host \"\";\n" +
            "           Write-Host \"group-header: VersionInfo\";\n" +
            "           Write-Host \"Component:\" $key;\n" +
            "           Write-Host \"FilePath:\" $try1;\n" +
            "           Write-Host (\"{0}.FileVersion: {1}\" -f $key, $ver.FileVersion);\n" +
            "           $ver | Format-List FileVersion,FileDescription,ProductName,ProductVersion,ProductMajorPart,ProductMinorPart,ProductBuildPart,ProductPrivatePart,FileMajorPart,FileMinorPart,FileBuildPart,FilePrivatePart;\n" +
            "           # Write-Host '!' $prog1 'ver is ' $ver;\n" +
            "           $done = $true;\n" +
            "         }\n" +
            "       }\n" +
            "      }\n" +
            "     }\n" +
            "     if (-not $done)\n" +
            "     {\n" +
            "      # Write-Host \"Guid is known, but version is not\"\n" +
            "      try {\n" +
            "       # $try2 = ( [System.Type]::GetTypeFromProgID( $progId )).Assembly.Location;\n" +
            "       # Write-Host \"TRY2 !!!!!!!!!!!!!\" $try2\n" +
            "      }\n" +
            "      catch {}\n" +
            "     }\n" +
            "    } \n" +
            "    else \n" +
            "    {\n" +
            "      Write-Host 'guid of *' $progid '* is not found';\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "Write-Host \"group-header: Processor\";\n" +
            "Get-WmiObject -Class Win32_Processor | Format-List Name,L2CacheSize,L3CacheSize,MaxClockSpeed;\n" +
            "\n" +
            "Write-Host \"group-header: ComputerSystem\";\n" +
            "Get-WmiObject -Class Win32_ComputerSystem | Format-List Manufacturer,Model,TotalPhysicalMemory;\n" +
            "\n" +
            "Write-Host \"group-header: OperatingSystem\";\n" +
            "Get-WmiObject -Class Win32_OperatingSystem | Format-List Caption,ServicePackMajorVersion,OSArchitecture,TotalVirtualMemorySize,TotalVisibleMemorySize,FreePhysicalMemory,FreeVirtualMemory,FreeSpaceInPagingFiles;\n" +
            "\n" +
            "GetComImplementation \"MDAC\"           \"ADODB.Connection\"                                     ;\n" +
            "GetComImplementation \"MSI\"            \"WindowsInstaller.Installer\"                           ;\n" +
            "GetComImplementation \"IE\"             \"InternetExplorer.Application\"                         ;\n" +
            "\n" +
            "  GetComImplementation \"FlashPlayer\"  \"ShockwaveFlash.ShockwaveFlash\"                        ;\n" +
            "  GetComImplementation \"Excel\"        \"Excel.Application\"                                    ;\n" +
            "  GetComImplementation \"Word\"         \"Word.Application\"                                     ;\n" +
            "  GetComImplementation \"Outlook\"      \"Outlook.Application\" # Not Found :(                   ;\n" +
            "  GetComImplementation \"PowerPoint\"   \"PowerPoint.Application\"                               ;\n";

    static {
        PS1 = PS1_Hard.replace("\n", System6.lineSeparator());
    }


}
