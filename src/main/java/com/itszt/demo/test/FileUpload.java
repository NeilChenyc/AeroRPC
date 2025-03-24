package com.itszt.demo.test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUpload {
    public static void main(String[] args) {
        String str = uploadFile("C:\\Users\\Administrator\\Desktop\\企业名称模板20210115154204.xlsx", "http://127.0.0.1:9985/api/grade/query/queryEnt", "企业名称模板20210115154204.xlsx");
        System.out.println(str);
    }

    /**
     *
     * @param file
     *            待上传的文件路径
     * @param uploadUrl
     *            上传服务接口路径
     * @param fileName
     *            文件名称，服务器获取的文件名称
     * @return
     */
    public static String uploadFile(/* Bitmap src */String file, String uploadUrl, String fileName) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            // name相当于html标签file的name属性，fileName相当于标签value值
            dos.writeBytes("Content-Disposition: form-data;name=\"selectFile\";fileName=\"" + fileName + "\"" + end);
            dos.writeBytes(end);

            // 将要上传的内容写入流中
            // InputStream srcis = Function.Bitmap2IS(src);
            InputStream srcis = new FileInputStream(file);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = srcis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            srcis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            // 上传返回值
            String sl;
            String result = "";
            while ((sl = br.readLine()) != null)
                result = result + sl;
            br.close();
            is.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "网络出错!";
        }
    }
}
