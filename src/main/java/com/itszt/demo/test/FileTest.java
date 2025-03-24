package com.itszt.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
@Slf4j
public class FileTest {

    public static void main(String[] args) {
        uploadFile("http://127.0.0.1:9985/api/grade/query/queryEnt","C:\\Users\\Administrator\\Desktop\\企业名称模板20210115154204.xlsx");

    }

    public static void uploadFile(String postUrl, String filePath){
        File postFile=new File(filePath);
        long length=postFile.length();
        String lengths = Long.toString(length);
        Map<String,String> postParam=new HashMap<String,String>();
        postParam.put("chunk_offset","0");
        postParam.put("chunk_size",lengths);
        Map<String,Object> resultMap = new HashMap<String,Object>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try{
            //把一个普通参数和文件上传给下面这个地址    是一个servlet
            HttpPost httpPost = new HttpPost(postUrl);
            httpPost.addHeader("Authorization", "Token 666abbf788ef1aacdb76a4f711509a8b391b6c8d");
            //把文件转换成流对象FileBody
            FileBody fundFileBin = new FileBody(postFile);
            //设置传输参数
            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
            multipartEntity.addPart("file", fundFileBin);//相当于<input type="file" name="media"/>
            //设计文件以外的参数
            Set<String> keySet = postParam.keySet();
            /* for (String key : keySet) {
                //相当于<input type="text" name="name" value=name>
                multipartEntity.addPart(key, new StringBody(postParam.get(key), ContentType.create("text/plain", Consts.UTF_8)));
             }  */
            multipartEntity.addPart("chunk_offset", new StringBody("0", ContentType.create("text/plain", Consts.UTF_8)));
            multipartEntity.addPart("chunk_size", new StringBody(lengths, ContentType.create("text/plain", Consts.UTF_8)));

            HttpEntity reqEntity =  multipartEntity.build();
            httpPost.setEntity(reqEntity);

            log.info("发起请求的页面地址 " + httpPost.getRequestLine());
            //发起请求   并返回请求的响应
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                log.info("----------------------------------------");
                //打印响应状态
                log.info(String.valueOf(response.getStatusLine()));
                resultMap.put("statusCode", response.getStatusLine().getStatusCode());
                //获取响应对象
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    //打印响应长度
                    log.info("Response content length: " + resEntity.getContentLength());
                    //打印响应内容
                    resultMap.put("data", EntityUtils.toString(resEntity, Charset.forName("UTF-8")));
                }
                //销毁
                EntityUtils.consume(resEntity);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally{
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("uploadFileByHTTP result:"+resultMap);
    }
}
