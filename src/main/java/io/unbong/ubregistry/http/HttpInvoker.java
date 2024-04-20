package io.unbong.ubregistry.http;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-03-21 22:46
 */
public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker Default = new OkHttpInvoker(500);
    String post(String requestString, String url);
    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz){

        log.debug("---> url: {}", url);

        String respJson = Default.get(url);
        log.debug("http get response json: {}", respJson);
        return JSON.parseObject(respJson, clazz);
    }

    @SneakyThrows
    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String respJson = Default.get(url);
        log.debug(" =====>>>>>> response: " + respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    static <T> T httpPost(String requestJson, String url, Class<T> clazz){
        log.debug(" =====>>>>>> httpPost url:{}, requestJson:{}" + url, requestJson);
        String respJson = Default.post(requestJson,url);
        log.debug(" =====>>>>>> response: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
