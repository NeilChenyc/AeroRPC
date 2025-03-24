package com.itszt.demo.nio;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Mark老师   享学课堂 https://enjoy.ke.qq.com
 * 类说明：
 */
public class Const {
    public static int DEFAULT_PORT = 8888;
    public static String DEFAULT_SERVER_IP = "127.0.0.1";

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /*根据输入信息拼接出一个应答信息*/
    public static String response(String msg) {
        return "Hello," + msg + ",Now is " + simpleDateFormat.format(new Date());
    }


}
