package org.universe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/5/13
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleTable {

    ArrayList<String> Captions = new ArrayList<String>();
    ArrayList<ArrayList<String>> Cells = new ArrayList<ArrayList<String>>();

    public void setColumns(String... captions)
    {
        Captions = new ArrayList<String>(Arrays.asList(captions));
    }

    public ConsoleTable() {
    }

    public ConsoleTable(String... captions) {
        setColumns(captions);
    }

    public ConsoleTable put(String cellValue)
    {
        ArrayList<String> row = getLastRow();
        row.add(cellValue);
        return this;
    }

    public ConsoleTable put(Object cellValue)
    {
        return put(cellValue == null ? "" : String.valueOf(cellValue));
    }

    public ConsoleTable skipCell()
    {
        ArrayList<String> row = getLastRow();
        row.add("");
        return this;
    }

    public ConsoleTable newRow()
    {
        Cells.add(new ArrayList<String>());
        return this;
    }

    @Override
    public String toString() {
        return toString('-', " | ");
    }

    public String toString(char headerRowSeparator, String columnSeparator)
    {
        ArrayList<ArrayList<String>> copy = new ArrayList<ArrayList<String>>();

        // header
        copy.add(Captions);
        int columns = Math.max(Captions.size(), getColumnCountOfData());

        // header separator
        ArrayList<String> headerSeparator = new ArrayList<String>();
        copy.add(headerSeparator);
        for(int i=0; i<columns; i++)
        {
            String caption = i < Captions.size() ? Captions.get(i) : "";
            StringBuilder b = new StringBuilder();
            int l = Math.max(2, caption.length());
            for(int j=0; j<l; j++) b.append(headerRowSeparator);
            headerSeparator.add(b.toString());
        }

        // Content
        for(int r = 0; r<Cells.size(); r++)
        {
            if (!(Cells.get(r).size() == 0 && r == Cells.size() - 1))
                copy.add(Cells.get(r));
        }

        // Widths
        int[] widths = new int[columns];
        for(int r = 0; r < copy.size(); r++)
        {
            List<String> row = copy.get(r);
            for(int c = 0; c < columns; c++)
            {
                String cell = c < row.size() ? row.get(c) : "";
                widths[c] = Math.max(1,Math.max(cell.length(), widths[c]));
            }
        }

        // output
        StringBuilder ret = new StringBuilder();
        for(int r = 0; r < copy.size(); r++)
        {
            StringBuilder b = new StringBuilder();
            List<String> row = copy.get(r);
            for(int c = 0; c < columns; c++)
            {
                String cell = c < row.size() ? row.get(c) : "";
                b.append(String.format("%-" + widths[c] + "s", cell));
                if (c+1 < columns)
                b.append(columnSeparator);
            }

            ret.append(b).append(System6.lineSeparator());
        }

        return ret.toString();
    }

    ArrayList<String> getLastRow()
    {
        if (Cells.size() == 0)
            Cells.add(new ArrayList<String>());

        return Cells.get(Cells.size() - 1);
    }

    int getColumnCountOfData()
    {
        int columns = 0;
        for(List<String> x : Cells)
            columns = Math.max(columns, x.size());

        return columns;
    }
}
