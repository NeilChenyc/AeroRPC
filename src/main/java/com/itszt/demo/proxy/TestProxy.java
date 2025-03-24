package com.itszt.demo.proxy;


import com.itszt.demo.proxy.impl.UserDao;

public class TestProxy {


    public static void main(String[] args) {
        //目标对象
        UserDao userDao = new UserDao();
        //代理对象
        UserDaoProxy userDaoProxy = new UserDaoProxy(userDao);
        userDaoProxy.save();
    }
}
