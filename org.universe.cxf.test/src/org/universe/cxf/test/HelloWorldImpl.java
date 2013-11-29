package org.universe.cxf.test;

import org.universe.tests.jaxb.MyVo1;
import org.universe.tests.jaxb.VoEnv;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "org.universe.cxf.test.HelloWorld",
        serviceName = "HelloWorld")
public class HelloWorldImpl implements HelloWorld {

    List<User> users = new ArrayList<User>();

    public String sayHi(String text) {
        // System.out.println("sayHi(String) called");
        return "Hello " + text + ". You secret lang is " + MyContextOnServer.getLang();
    }

    @Override
    public String sayHi(int arg) {
        // System.out.println("sayHi(Int) called");
        return "Hello " + arg + ". You secret lang is " + MyContextOnServer.getLang();
    }

    public String sayHiToUser(User user) {
        // System.out.println("sayHiToUser called");
        users.add(user);
        return "Hello "  + user.getName();
    }

    public List<User> getUsers() {
        // System.out.println("getUsers called");
        return users;
    }

    @Override
    public String returnCopyOfSecretHeader() {
        return MyContextOnServer.getLang();
    }

    @Override
    public MyVo1 getMyVo1() {
        return VoEnv.createVO1();
    }
}
