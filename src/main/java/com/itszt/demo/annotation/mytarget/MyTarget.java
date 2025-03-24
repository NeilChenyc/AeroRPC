package com.itszt.demo.annotation.mytarget;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Retention注解有一个属性value,是Retention类型,
 * Enum的RetentionPolicy是一个枚举类型的
 *
 *  java用@interface Annotation {} 定义一个注解@Annotation ,一个注解是一个类
 *
 * @Override，@Deprecated，@SuppressWarnings为常见的3个注解。
 *
 * 注解@Override用在方法上,当我们想重写一个方法时,在方法上加上@Override,当我们方法的名字出错时,编译器就会报错
 *
 * 注解@Deprecated,用来的表示某个类的属性或方法已经过时,不想别人再用时,在属性和方法上使用@Deprecated
 *
 * 注解@SuppressWarnings用来压制程序中出现的警告,比如在没有用泛型或是方法已经过时的时候
 *
 * 注解@Retention可以用来修饰注解,是注解的注解,称为元注解
 *
 * Retention注解有一个属性value,是RetentionPolicy类型的,Enum RetentionPolicy 是一个枚举类型,
 *
 * 这个枚举决定了Retention注解应该如何去保持,也可以理解为Retention搭配RetentionPolicy使用。
 *
 * RetentionPolicy有3个值: CLASS RUNTIME SOURCE
 *
 * @Retention(RetentionPolicy.CLASS)修饰的注解,表示注解的信息被保留在class文件(字节码文件)中当程序编译时,但不会被虚拟机读取在运行的时候
 *
 * 用@Retention(RetentionPolicy.SOURCE )修饰的注解,表示注解的信息会被编译器抛弃，不会留在class文件中，注解的信息只会留在源文件中
 *
 * 用@Retention(RetentionPolicy.RUNTIME )修饰的注解，表示注解的信息被保留在class文件(字节码文件)中当程序编译时，会被虚拟机保留在运行时
 *
 * 所以他们可以用反射的方式读取。RetentionPolicy.RUNTIME 可以让你从JVM中读取Annotation注解的信息，以便在分析程序的时候使用.
 *
 * 如果注解中有一个属性名字叫value,则在应用时可以省略属性名字不写
 *
 *
 *
  *  public @interface MyTarget
  *  {
  *  String value();
  *  }
  *
*      @MyTarget("aaa")  直接省略写value参数名
 *  public void doSomething()
 *  {
 *   System.out.println("hello world");
 *  }
 *
 *   注解@Target也是用来修饰注解的元注解，它有一个属性ElementType也是枚举类型，
 *
 * 值为：ANNOTATION_TYPE CONSTRUCTOR  FIELD LOCAL_VARIABLE METHOD PACKAGE PARAMETER TYPE
 *
 *
 *   如@Target(ElementType.METHOD) 修饰的注解表示该注解只能用来修饰在方法上
 *
 *   用来标识方法修饰的范围的
 *
 *
 *
 *
 *
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTarget {
}
