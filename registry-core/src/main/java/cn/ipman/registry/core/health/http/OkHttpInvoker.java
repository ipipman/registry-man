package cn.ipman.registry.core.health.http;

import cn.ipman.registry.core.health.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 使用OkHttp实现的HTTP调用器，提供POST和GET方法的实现。
 *
 * @Author IpMan
 * @Date 2024/4/14 20:04
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    /**
     * 构造函数，初始化OkHttpClient配置。
     *
     * @param timeout 连接、读写超时时间（毫秒）
     */
    public OkHttpInvoker(int timeout) {
        // 配置OkHttpClient，包括连接池、超时设置和失败重试等
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS)) // 连接池配置
                .readTimeout(timeout, TimeUnit.MILLISECONDS)  // 读超时时间
                .writeTimeout(timeout, TimeUnit.MILLISECONDS) // 写超时时间
                .connectTimeout(timeout, TimeUnit.MILLISECONDS) // 连接超时时间
                .retryOnConnectionFailure(true) // 运行失败重连
                .build();

    }

    /**
     * 执行HTTP POST请求。
     *
     * @param requestString 请求体字符串
     * @param url 请求的URL
     * @return 返回HTTP响应体的字符串内容
     */
    @Override
    public String post(String requestString, String url) {
        log.debug(" ===> post  url = {}, requestString = {}", requestString, url);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSON_TYPE))
                .build();
        try {
            String respJson = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            //log.error("okHttp post error:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行HTTP GET请求。
     *
     * @param url 请求的URL
     * @return 返回HTTP响应体的字符串内容
     */
    @Override
    public String get(String url) {
        log.debug(" ===> get url = " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
