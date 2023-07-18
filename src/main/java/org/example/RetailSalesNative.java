package org.example;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RetailSalesNative {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    public static void main(String[] args) throws IOException {
        Long internal = Long.parseLong(args[0]);
        Map<String, String> hd = new HashMap<>();
        hd.put("authority", "www.census.gov");
        hd.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        hd.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7");
        hd.put("cache-control", "no-cache");
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
                .url("https://www.census.gov/retail/sales.html")
                .headers(Headers.of(hd))
                .build();
        while (true) {
            try {
                Long now = System.currentTimeMillis();
                Response response = httpClient.newCall(request).execute();
                String body = response.body().string();
//                System.out.println(body);
                log.info("RetailSalesNative download finish, cost: {}ms", System.currentTimeMillis() - now);
                Document document = Jsoup.parse(body, "https://www.census.gov/retail/sales.html");
                String title = document.title();
                log.info("RetailSalesNative html title: {}", title);
                Elements elements = document.body().select("div.publicationdate.publishdate.parbase");
                String time = elements.get(0)
                        .select("div.uscb-margin-TB-5").get(0)
                        .getElementsByTag("time").get(0)
                        .text();
                log.info("RetailSalesNative parse, key: {}", time);
                log.info("RetailSalesNative parse finish, cost: {}ms", System.currentTimeMillis() - now);

            } catch (Exception e) {
                log.error("RetailSalesNative error", e);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(internal);
            } catch (InterruptedException e) {

            }

        }
    }
}
