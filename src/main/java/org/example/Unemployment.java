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
public class Unemployment {

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final static Long INTERVAL = 700L;
    private final static String URL = "https://www.dol.gov/newsroom/releases?agency=All&state=All&topic=132&year=all&page=0";


    public static void main(String[] args) {
        Map<String, String> hd = new HashMap<>();
        hd.put("authority", "www.dol.gov");
        hd.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        hd.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7");
        hd.put("cache-control", "no-cache");
//        hd.put("cookie", "cebs=1; _ce.s=v~98a4f0e1a329c067ab842cf498b984ab47427be4~lcw~1689653954057~vpv~0~lcw~1689653954058; _gid=GA1.2.1845983444.1689848852; _ce.clock_event=1; _ce.clock_data=46%2C115.236.15.78%2C1%2C5f0ff5d8799ed4c0ed355fa474a7bbc2; _ga_988WLCRHJJ=GS1.1.1689848852.3.1.1689849024.0.0.0; _ga_LTGX1LSENR=GS1.1.1689848851.3.1.1689849024.0.0.0; _ga=GA1.1.497560074.1689653954; _ga_8D6G2LK2BW=GS1.1.1689848852.3.1.1689849024.0.0.0; _ga_HZ8Y6WJF2K=GS1.1.1689848852.3.1.1689849024.0.0.0; cebsp_=11");
        hd.put("pragma", "no-cache");
        hd.put("sec-ch-ua", "'Not.A/Brand';v='8', 'Chromium';v='114', 'Google Chrome';v='114");
        hd.put("sec-ch-ua-mobile", "?0");
        hd.put("sec-ch-ua-platform", "macOS");
        hd.put("sec-fetch-dest", "document");
        hd.put("sec-fetch-mode", "navigate");
        hd.put("sec-fetch-site", "none");
        hd.put("sec-fetch-user", "?1");
        hd.put("upgrade-insecure-requests", "1");
        hd.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");


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
                log.error("Unemployment error", e);
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            Long cost = System.currentTimeMillis() - now;
            log.info("Unemployment parse finish, cost: {}ms", cost);
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
        log.info("Unemployment download finish, cost: {}ms", System.currentTimeMillis() - start);
        Document document = Jsoup.parse(body, URL);
        Elements list = document.body().select("div.image-left-teaser");
        for (Element element : list) {
            if (element.text().contains("Unemployment Insurance Weekly Claims Report")) {
                String keyword = element.select("p.dol-date-text").get(0).text();
                log.info("Unemployment parse, keyword: {}", keyword);
                break;
            }
        }

    }
}
