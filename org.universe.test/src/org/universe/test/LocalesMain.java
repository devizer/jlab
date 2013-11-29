package org.universe.test;

import org.universe.ConsoleTable;
import org.universe.System6;

import java.util.*;

public class LocalesMain
{
    public static void main(String... args)
    {

        Properties x = System.getProperties();
        System.out.println(x);
        Set<String> ss = x.stringPropertyNames();
        SortedSet<String> sss = new TreeSet<String>(ss);
        for(String y : sss)
            System.out.println(y + ":" + x.getProperty(y));


        List<Locale> locs = Arrays.asList(Locale.getAvailableLocales());
        Collections.sort(locs, new Comparer());

        ConsoleTable t = new ConsoleTable("As Is", "Tag7", "Language", "Country", "Var", "Display Name", "Display Name (English)", "Display Name (Native)", "Country", "ISO3 Country", "ISO3 Lang");
        for(Locale l: locs)
        {
            String iso3Country = ":(";
            try { iso3Country = l.getISO3Country(); }
            catch(Exception ex) {}

            String nativeName = l.getDisplayName(l);
            t       .put(l.toString())
                    .put(System6.toLocaleTag(l))
                    .put(l.getLanguage())
                    .put(l.getCountry())
                    .put(l.getVariant())
                    .put(l.getDisplayName())
                    .put(l.getDisplayName(new Locale("en-US")))
                    .put(nativeName)
                    .put(l.getCountry())
                    .put(iso3Country)
                    .put(l.getISO3Language())
                    // .put(l.getDisplayScript())
                    .put(l.getDisplayVariant())
                    .newRow();
        }

        System.out.println("Locales");
        System.out.println(t);

    }

    static class Comparer implements Comparator<Locale> {
        @Override
        public int compare(Locale o1, Locale o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString());
        }
    }
}
