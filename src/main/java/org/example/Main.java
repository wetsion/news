package org.example;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    public static void main(String[] args) throws IOException {
        while (true) {
            try {
                Long now = System.currentTimeMillis();
                Map<String, String> hd = new HashMap<>();
                hd.put("authority", "www.bls.gov");
                hd.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                hd.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7");
                hd.put("cache-control", "no-cache");
                hd.put("cookie", "nmstat=ca784205-fae6-a660-bd14-a478e665ccc2; _ga_WFFDEGRMJE=GS1.1.1689046537.3.0.1689046537.0.0.0; _ga=GA1.2.1992014291.1688378116; _gid=GA1.2.1280913940.1689046538; _gat_GSA_ENOR0=1");
                hd.put("pragma", "no-cache");
                hd.put("sec-ch-ua", "'Not.A/Brand';v='8', 'Chromium';v='114', 'Google Chrome';v='114'");
                hd.put("sec-ch-ua-mobile", "?0");
                hd.put("sec-ch-ua-platform", "macOS");
                hd.put("sec-fetch-dest", "document");
                hd.put("sec-fetch-mode", "navigate");
                hd.put("sec-fetch-site", "none");
                hd.put("sec-fetch-user", "?1");
                hd.put("upgrade-insecure-requests", "1");
                hd.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                Request request = new Request.Builder()
                        .url("https://www.bls.gov/news.release/empsit.nr0.htm")
                        .headers(Headers.of(hd))
                        .build();
                Response response = httpClient.newCall(request).execute();
                String body = response.body().string();
                log.info(body);
                log.info("pachong finish, cost: {}ms", System.currentTimeMillis() - now);
                if (body.contains("THE EMPLOYMENT SITUATION")) {
                    log.info("pachong success");
                }
                TimeUnit.MILLISECONDS.sleep(500L);
            } catch (InterruptedException e) {

            }

        }
    }
}