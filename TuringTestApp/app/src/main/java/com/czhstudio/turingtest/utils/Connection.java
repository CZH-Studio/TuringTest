package com.czhstudio.turingtest.utils;

import com.google.gson.Gson;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

import java.lang.reflect.Type;

public class Connection {
    /** 完整版的get请求
     * @param url 请求的url
     * @param entity 请求的实体
     * @param mode 实体翻译成字符串的模式
     * @param timeout 超时时间
     * @return 返回请求的响应体
     */
    public static String get(String url, Entity entity, int mode, int timeout){
        // 调试模式打开时，使用本地json内容
        url += entity.toGet(mode);
        System.out.printf("Get: %s%n", url);
        String body = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpGet.setConfig(RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build());
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            body = EntityUtils.toString(response.getEntity());
            response.close();
        } catch (Exception e) {
            return body;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                body = null;
            }
        }
        System.out.printf("Get response: %s%n", body);
        return body;
    }

    /**
     * 简化版的get请求（超时时间使用默认3000ms）
     * @param url 请求的url
     * @param entity 请求的实体
     * @param mode 实体翻译成字符串的模式
     * @return 返回请求的响应体
     */
    public static String get(String url, Entity entity, int mode) {
        /* 默认超时时间的get请求，只需要指定url、实体和翻译成字符串的模式 */
        return get(url, entity, mode, 3000);
    }

    /**
     * 简化版的get请求（超时时间使用默认3000ms，翻译模式默认0）
     * @param url 请求的url
     * @param entity 请求的实体
     * @return 返回请求的响应体
     */
    public static String get(String url, Entity entity) {
        /* 更简洁的get请求方式，只需要指定url和实体，翻译成字符串的模式如果只有1个可以使用 */
        return get(url, entity, 0, 3000);
    }

    public static String post(String url, Entity entity, int mode, int timeout) {
        /* post方法 */
        // 调试模式打开时，使用本地json内容
        String sendData = entity.toPost(mode);
        System.out.printf("Post: %s, Data: %s%n", url, sendData);
        String body = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build());
        try {
            StringEntity s = new StringEntity(sendData, "UTF-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            httpPost.setEntity(s);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            CloseableHttpResponse response = client.execute(httpPost);
            //获取结果实体
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                //按指定编码转换结果实体为String类型，应该是一个json字符串
                 body = EntityUtils.toString(httpEntity, "UTF-8");
            }
            EntityUtils.consume(httpEntity);
            response.close();
            client.close();
        } catch (Exception e) {
            return body;
        }
        System.out.printf("Post response: %s%n", body);
        return body;
    }

    public static String post(String url, Entity entity, int mode){
        /*  */
        return post(url, entity, mode, 3000);
    }

    public static String post(String url, Entity entity){
        return post(url, entity, 0, 3000);
    }

    public static <T> T parse(String body, Type type){
        /* 解析json */
        try{
            return new Gson().fromJson(body, type);
        } catch (Exception e){
            return null;
        }
    }
}
