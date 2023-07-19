package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.tool.RedisUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.redisson.api.RLock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LeafHandlerApi {

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final static Long INTERVAL = 700L;

    private final static String URL = "https://api.eia.gov/v2/petroleum/sum/sndw/data/?api_key=CZdQsisRJzwOfqUWV3jiMPNEx3ZbHcuJ2VQus04i";


    public static void main(String[] args) {
        Map<String, String> hd = new HashMap<>();
        hd.put("authority", "api.eia.gov");
        hd.put("accept", "application/json, text/plain, */*");
        hd.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7");
        hd.put("cache-control", "no-cache");
        hd.put("content-type", "application/json");
        hd.put("origin", "https://www.eia.gov");
        hd.put("pragma", "no-cache");
        hd.put("referer", "https://www.eia.gov/");
        hd.put("sec-ch-ua", "'Not.A/Brand';v='8', 'Chromium';v='114', 'Google Chrome';v='114'");
        hd.put("sec-ch-ua-mobile", "?0");
        hd.put("sec-ch-ua-platform", "macOS");
        hd.put("sec-fetch-dest", "empty");
        hd.put("sec-fetch-mode", "cors");
        hd.put("sec-fetch-site", "same-site");
        hd.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        hd.put("x-params", "{\"frequency\":\"weekly\",\"data\":[\"value\"],\"facets\":{\"series\":[\"WCESTUS1\"]},\"start\":\"2023-07-10\",\"end\":null,\"sort\":[{\"column\":\"period\",\"direction\":\"desc\"}],\"offset\":0,\"length\":5000}");

        Request request = new Request.Builder()
                .url(URL)
                .headers(Headers.of(hd))
                .build();

        while (true) {
            Long now = System.currentTimeMillis();
            RLock lock = RedisUtil.redissonClient().getLock("LeafHandler");
            try {
                if (lock.tryLock(10L, 60L, TimeUnit.MILLISECONDS)) {
                    doCrawler(request, now);
                }
            } catch (Exception e) {
                log.error("LeafHandler error", e);
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            Long cost = System.currentTimeMillis() - now;
            log.info("LeafHandler parse finish, cost: {}ms", cost);
            if (cost < INTERVAL) {
                try {
                    TimeUnit.MILLISECONDS.sleep(INTERVAL - cost);
                } catch (InterruptedException e) {}
            }
        }
    }

    private static void doCrawler(Request request, Long start) throws IOException {
        Response response = httpClient.newCall(request).execute();
        String body = response.body().string();
        log.info("LeafHandler download finish, cost: {}ms", System.currentTimeMillis() - start);
        JSONObject jsonObject = JSON.parseObject(body);
        JSONObject res  = jsonObject.getJSONObject("response");
        int total = res.getIntValue("total");
        if (total > 0) {
            JSONArray data = res.getJSONArray("data");
            int value = data.getJSONObject(0).getIntValue("value");
            log.info("LeafHandler parse, key: {}", value);
        } else {
            log.info("LeafHandler parse, no update");
        }
    }
}
