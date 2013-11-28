package org.universe.sql;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 10/17/13
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqlCodeReader
{
    // Read sql batch and split by /*EXEC*/ separator
    public static List<String> SplitSqlResources(String path) throws IOException
    {
        InputStream inp1 = SqlCodeReader.class.getResourceAsStream(path);
        if (inp1 == null)
            throw new IOException("Embedded resource " + path + " is expected");

        List<String> ret = new ArrayList<String>();
        LineIterator iter = IOUtils.lineIterator(inp1, "utf-8");
        StringWriter buffer = new StringWriter();
        while(iter.hasNext())
        {
            String line = iter.nextLine();
            if (line.trim().toUpperCase().startsWith("/*EXEC")
                    || line.trim().toUpperCase().equals("GO"))
            {
                if (buffer.toString().trim().length() > 0) ret.add(buffer.toString());
                buffer = new StringWriter();
            }
            else
            {
                buffer.write("\r\n");
                buffer.write(line);
            }
        }

        if (buffer.toString().trim().length() > 0) ret.add(buffer.toString());
        return ret;
    }


}


