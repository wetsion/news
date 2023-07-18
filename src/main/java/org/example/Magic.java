package org.example;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.concurrent.TimeUnit;

public class Magic implements PageProcessor {

    private Site site = Site.me().setRetryTimes(1).setSleepTime(0)
            .setTimeOut(3000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            .addHeader("authority", "www.bls.gov")
            .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7")
            .addHeader("cache-control", "no-cache")
            .addHeader("cookie", "nmstat=ca784205-fae6-a660-bd14-a478e665ccc2; _ga_WFFDEGRMJE=GS1.1.1689046537.3.0.1689046537.0.0.0; _ga=GA1.2.1992014291.1688378116; _gid=GA1.2.1280913940.1689046538; _gat_GSA_ENOR0=1")
            .addHeader("pragma", "no-cache")
            .addHeader("sec-ch-ua", "'Not.A/Brand';v='8', 'Chromium';v='114', 'Google Chrome';v='114'")
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("sec-ch-ua-platform", "macOS")
            .addHeader("sec-fetch-dest", "document")
            .addHeader("sec-fetch-mode", "navigate")
            .addHeader("sec-fetch-site", "none")
            .addHeader("sec-fetch-user", "?1")
            .addHeader("upgrade-insecure-requests", "1")
            .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

    @Override
    public void process(Page page) {
        System.out.println(page.getRawText());
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        while (true) {
            Spider.create(new Magic())
                    .addUrl("https://www.bls.gov/news.release/empsit.nr0.htm")
                    .thread(1)
                    .run();
            try {
                TimeUnit.MILLISECONDS.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
