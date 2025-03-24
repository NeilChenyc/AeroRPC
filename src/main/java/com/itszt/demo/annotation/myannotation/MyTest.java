package com.itszt.demo.annotation.myannotation;

@MyAnnotation(hello = "beijing", world="shanghai",array={},style=int.class)
public class MyTest {


    @MyAnnotation(world = "shanghai",array={1,2,3})
    @Deprecated
    @SuppressWarnings("")
    public void output()
    {
        System.out.println("output something!");
    }
}
