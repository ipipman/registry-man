package cn.ipman.registry.health;

import cn.ipman.registry.health.http.OkHttpInvoker;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;

public interface HttpInvoker {

    HttpInvoker Default = new OkHttpInvoker(300);

    String post(String requestString, String url);

    String get(String url);

    @SneakyThrows
    static <T> T httpGet(String url, Class<T> clazz) {
        System.out.println(" =====>>>>>> httpGet: " + url);
        String respJson = Default.get(url);
        System.out.println(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

    @SneakyThrows
    static <T> T httpPost(String requestString, String url, Class<T> clazz) {
        System.out.println(" =====>>>>>> httpPost: " + url);
        String respJson = Default.post(requestString, url);
        System.out.println(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
