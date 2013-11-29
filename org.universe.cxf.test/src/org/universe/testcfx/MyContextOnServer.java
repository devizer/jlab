package org.universe.testcfx;

class MyContextOnServer
{
    static ThreadLocal<String> Lang = new ThreadLocal<String>();

    static String getLang() {
        return Lang.get();
    }

    static void setLang(String lang)
    {
        Lang.set(lang);
    }
}
