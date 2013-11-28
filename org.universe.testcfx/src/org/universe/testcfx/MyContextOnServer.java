package org.universe.testcfx;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/5/13
 * Time: 8:39 PM
 * To change this template use File | Settings | File Templates.
 */
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
