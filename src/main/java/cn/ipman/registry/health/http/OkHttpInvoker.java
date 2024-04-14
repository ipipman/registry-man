package cn.ipman.registry.health.http;

import cn.ipman.registry.health.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/14 20:04
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        // 用okHttp进行远程传输
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true) // 运行失败重连
                .build();

    }

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
