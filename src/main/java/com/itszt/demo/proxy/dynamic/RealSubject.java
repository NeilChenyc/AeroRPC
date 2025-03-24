package com.itszt.demo.proxy.dynamic;

public class RealSubject implements Subject {
    @Override
    public void doSomething() {
        System.out.println("call doSomething()");
    }
}
