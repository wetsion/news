package org.example;

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
public class LeafHandler {

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final static Long INTERVAL = 700L;
    private final static String URL = "https://www.eia.gov/dnav/pet/hist/LeafHandler.ashx?n=PET&s=WCESTUS1&f=W";

    public static void main(String[] args) {
        Map<String, String> hd = new HashMap<>();
        hd.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        hd.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7");
        hd.put("Cache-Control", "no-cache");
        hd.put("Connection", "keep-alive");
        hd.put("Cookie", "_gid=GA1.2.1763394620.1689747115; bm_mi=D787BC954271DCA32124EDD7148D8D23~YAAQCS0+F4DGWVWJAQAAmld8bRRgpk30FU1cuZRYRRLBdfQaGf7guo1wGOkYk/kJpTAYh5Ax4AXJmxx9XNekEfQ3H2r+4nAAdiRch/6kqffnDMqMuCEnybT/6HsjnQwqaTK22KdIeUBVvVM3lJNCqKCRREo6Bjvk89xx6Lf/lGYfK/keP2EShFc3+hVygALmCpWowyWEcsarunMJ7wEC7aSP4pZ7KyqVxdCBOVXUkbKI0N3Erq/HIR3ZzE5vJWL+DXmo/0zmS93+S5N4U1xZbWzTIU5qgqykuWB1tV2LkLjwfeOQFRWV2w9hkfpJyRAqxxqy/fAtsDmns2BEDdo/FcFBHO0L/5V+gNEGHAHloLfc~1; _ga_NB85F8V3TS=GS1.1.1689759015.2.0.1689759015.0.0.0; _ga=GA1.2.707309017.1689747115; ak_bmsc=F35F39296FB96224DFA874E46B5DAF98~000000000000000000000000000000~YAAQCS0+F3L8WVWJAQAAG/Z9bRRVGOari7+WTKd7DCQqHnhp0gnkZiKbzKYFHzfyWLjJQPeF79TafcyEBKZGRHPm0Wqarv/pg767lrDN75GpZdO05RVjujndNDCjWLvdf37encPq1bsSpAwEwZnpDwXwU4SrIE3dbjMkEx5a5S2yw7ScqWZRb4FDGhFSErDqoBh6gtHZ6GSRJQAvCNr6EIV0BeCU/ga2I8KCIL43PtHNnDUMcue+H0E20XX/M3t+5VrmCYEGJRcpgB8ISIqiaCN4u89gPeHErPb/TIxHehhB7a20H3qnEM9BmsliZ9QT+8m1gzMOzfXuB2TXvQEtlyk/A44qffY2pUq53nTgjaqxBKVDUCh/887K5KVCtRTHO4eWp7bt0f2fDMIcDdvdpiMUL0xG3XlotaphoF6QHY0j4zXWIybWqAx33HzSFXUkKq2ohzjxrjBLtSyQxKQ5wWXDDX8XeRRsybwCO3K2ojYyT2I7MFX/7gQHARFwh9wjpGrODfNpv3OsRZVgq5lZeWCawUKw+KXfl8AprJZe; bm_sv=36445CD4F30D26CBD3D3DD7BD9AABF51~YAAQCS0+F3P8WVWJAQAAG/Z9bRTH6LWgnjmdbzsDz4fprSc7V74d3IyNDoUaZtmtJOAi4B9rya+Bku7wzCOTrmczggtUGgobneX9hgQtB2M3t6Ywjm5vQanQAoLni/Azbvzsbz4qa4cGOkf0yPnyWbVF9a6ac8yF0qJUt7Wfa4bJD3GG0hUeYt68Ut/4JQWq6Azx7uvkh12tHs/wXFpUBomXKhwg7zq11owLusns6/SK/EA4/cw4JemT0bHc~1");
        hd.put("Pragma", "no-cache");
        hd.put("Sec-Fetch-Dest", "document");
        hd.put("Sec-Fetch-Mode", "navigate");
        hd.put("Sec-Fetch-Site", "cross-site");
        hd.put("Sec-Fetch-User", "?1");
        hd.put("Upgrade-Insecure-Requests", "1");
        hd.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        hd.put("sec-ch-ua", "'Not.A/Brand';v='8', 'Chromium';v='114', 'Google Chrome';v='114'");
        hd.put("sec-ch-ua-mobile", "?0");
        hd.put("sec-ch-ua-platform", "macOS");

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
        Document document = Jsoup.parse(body, URL);
        String pd = document.body().select("td.F2").get(1).text();
        log.info("LeafHandler parse, key: {}", pd);
    }
}
