package org.example;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RetailSales implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(10)
            .setTimeOut(3000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            .addHeader("authority", "www.census.gov")
            .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7")
            .addHeader("cache-control", "no-cache")
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
        Html html = page.getHtml();
        String time = html.xpath("//div[@class='publicationdate publishdate parbase']")
                .xpath("//div[@class='uscb-margin-TB-5']")
                .xpath("//time").toString().replaceAll("\\<.*?\\>", "");
        log.info("RetailSales publish time: {}", time);
        System.out.println(time);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        while (true) {
            Spider.create(new RetailSales())
                    .addUrl("https://www.census.gov/retail/sales.html")
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
