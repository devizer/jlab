package org.universe.io;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;

public class SimpleStreamIterator
{
    LineIterator iterator;
    Options options;

    public SimpleStreamIterator(File file, Options options) {
        this.options = options;
        try {
            iterator = IOUtils.lineIterator(new FileInputStream(file), "utf8");
        } catch (IOException e) {
            throw new RuntimeException("Failed to open line iterator", e);
        }
    }


    public SimpleStreamIterator(InputStream stream, Options options) {
        this.options = options;
        try {
            iterator = IOUtils.lineIterator(stream, "utf8");
        } catch (IOException e) {
            throw new RuntimeException("Failed to open line iterator", e);
        }
    }

    // returns null at end of stream
    // safe to multiple calls after end of stream
    // stream is closing at end of stream
    public Row next()
    {
        if (iterator == null)
            return null;

        if (!iterator.hasNext())
        {
            iterator = null;
            return null;
        }

        String line = iterator.nextLine();
        Row ret = new Row();
        ret.Line = line;
        Map.Entry<String, String> row = parseLine(line, options.keyValueSeparator);
        if (row != null)
        {
            String key = row.getKey();
            if (options.trimKey) key = key.trim();
            if (options.lowerKey) key = key.toLowerCase();
            ret.Key = key;
            ret.Value = options.trimValue ? row.getValue().trim() : row.getValue();
            ret.HasKey = true;
        }

        return ret;

    }

    // wnen cycle was broken
    // safe to call twice
    public void close()
    {
        if (iterator != null)
        {
            iterator.close();
            iterator = null;
        }
    }

    static Map.Entry<String,String> parseLine(String arg, String separator)
    {
        int index = arg.indexOf(separator);
        if (index > 0)
        {
            String key = arg.substring(0, index);
            String value = index + 1 < arg.length() ? arg.substring(index+1) : "";
            return new AbstractMap.SimpleImmutableEntry<String, String>(key, value);
        }

        return null;
    }

    public static class Options
    {
        String keyValueSeparator;
        boolean trimKey;
        boolean trimValue;
        boolean lowerKey;

        public Options(String keyValueSeparator, boolean trimKey, boolean trimValue, boolean lowerKey) {
            this.keyValueSeparator = keyValueSeparator;
            this.trimKey = trimKey;
            this.trimValue = trimValue;
            this.lowerKey = lowerKey;
        }
    }

    public static class Row
    {
        public String Line;
        public boolean HasKey;
        public String Key;
        public String Value;
    }
}
