package org.example;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.tool.RedisUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.api.RLock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Confidence {

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final static Long INTERVAL = 700L;
    private final static String URL = "https://www.conference-board.org/topics/consumer-confidence";

    public static void main(String[] args) {
        Map<String, String> hd = new HashMap<>();
        hd.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        hd.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7");
        hd.put("Cache-Control", "no-cache");
        hd.put("Connection", "keep-alive");
//        hd.put("Cookie", "CFID=Z39svg6vtzr1i31toovo39qbmdzhwjr9e17l88dm1qhd705bwfa-277792273; CFTOKEN=Z39svg6vtzr1i31toovo39qbmdzhwjr9e17l88dm1qhd705bwfa-7dc31e5e72a2115-7E17E2B9-FD7F-7752-710CF6E8DA068AEC; _gcl_au=1.1.42256390.1690281199; _ga=GA1.1.1518441029.1690281199; _ga_7W1QTDGJTL=GS1.1.1690281198.1.0.1690281198.60.0.0; ln_or=eyIyMzM0MTgiOiJkIiwiMjMzNDE4LDQ4NjA5OCI6ImQifQ%3D%3D; _mkto_trk=id:225-WBZ-025&token:_mch-conference-board.org-1690281265784-43512; MEMBERSHIPFLYOUTMSG=true; SHB=1");
        hd.put("Pragma", "no-cache");
        hd.put("Referer", "https://docs.qq.com/");
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
            RLock lock = RedisUtil.redissonClient().getLock("Unemployment");
            boolean getLock = false;
            try {
                if (lock.tryLock(0L, 60L, TimeUnit.MILLISECONDS)) {
                    getLock = true;
                    doCrawler(request, now);
                }
            } catch (Exception e) {
                log.error("Confidence error", e);
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            Long cost = System.currentTimeMillis() - now;
            log.info("Confidence parse finish, cost: {}ms", cost);
            if (getLock && cost < INTERVAL) {
                try {
                    TimeUnit.MILLISECONDS.sleep(INTERVAL - cost);
                } catch (InterruptedException e) {}
            }
        }
    }

    private static void doCrawler(Request request, Long start) throws IOException {
        Response response = httpClient.newCall(request).execute();
        String body = response.body().string();
        log.info("Confidence download finish, cost: {}ms", System.currentTimeMillis() - start);
        Document document = Jsoup.parse(body, URL);
        Elements list = document.body().select("p.date");
        String keyword = list.get(0).text();
        log.info("Confidence parse, keyword: {}", keyword);
    }
}
