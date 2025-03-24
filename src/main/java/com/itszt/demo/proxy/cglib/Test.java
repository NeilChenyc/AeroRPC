package com.itszt.demo.proxy.cglib;

public class Test {

    public static void main(String[] args) {
        Target target = new Target();
        Target proxyInstance = (Target) new ProxyFactory(target).getProxyInstance();
        proxyInstance.save();
    }
}
