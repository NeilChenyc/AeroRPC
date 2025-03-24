package com.itszt.demo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ENV {

    public static void main(String[] args) throws Exception {
        /***获取所有系统环境变量***/
        Map<String, String> map2 = System.getenv();
        for(Iterator<String> it = map2.keySet().iterator(); it.hasNext();){
            String key = it.next();
            System.out.println(key + "=" + map2.get(key));
        }
        /********随便修改一个变量*********/
        Map<String, String> map5 = new HashMap<>();
        map5.put("USERDOMAIN_ROAMINGPROFILE","1111111111");
        System.out.println("-------------------------------------------------------------------");
       /******执行修改方法*******/
        setEnv(map5);
        /*******遍历新的环境变量*******/
                Map<String, String> map4= System.getenv();
        for(Iterator<String> it = map4.keySet().iterator(); it.hasNext();){
            String key = it.next();
            System.out.println(key + "=" + map4.get(key));
        }
    }


    protected static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }
    }
