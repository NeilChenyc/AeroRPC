package com.itszt.demo.annotation.myannotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MyReflection {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MyTest myTest= new MyTest();
        Class<MyTest> myTestClass = MyTest.class;
        Method method = myTestClass.getMethod("output", null);
        if (myTestClass.isAnnotationPresent(MyAnnotation.class)){
            System.out.println("have  annotation!!!!");
        }
        if (myTestClass.isAnnotationPresent(MyAnnotation.class)){

           method.invoke(myTest, null);
            MyAnnotation annotation = method.getAnnotation(MyAnnotation.class);
            String hello = annotation.hello();
            System.out.println("hello = " + hello);
            int[] array = annotation.array();
            System.out.println("array = " + array);
            Class style = annotation.style();
            System.out.println("style = " + style);
            String world = annotation.world();
            System.out.println("world = " + world);

        }

        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            System.out.println("annotation = " + annotation.annotationType().getName());
        }
    }
}
