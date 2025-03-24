package com.itszt.demo.proxy.impl;

import com.itszt.demo.proxy.IUserDao;

public class UserDao implements IUserDao {
    @Override
    public void save() {
        System.out.println("保存数据");
    }
}
