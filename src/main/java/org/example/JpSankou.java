package org.example;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.tool.RedisUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.redisson.api.RLock;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JpSankou {

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final static Long INTERVAL = 400L;
    private final static String PDF_URL = "https://www.stat.go.jp/data/roudou/sokuhou/tsuki/pdf/202306sankou.pdf";
    private final static String HTML_URL = "https://www.stat.go.jp/data/roudou/sokuhou/tsuki/index.html";

    public static void main(String[] args) {

        while (true) {
            new Thread(JpSankou::pdf).start();
            new Thread(JpSankou::news).start();
            try {
                TimeUnit.MILLISECONDS.sleep(INTERVAL);
            } catch (InterruptedException e) {}
        }

    }

    private static void pdf() {
        Map<String, String> hd = new HashMap<>();

        Request request = new Request.Builder()
                .url(PDF_URL)
                .headers(Headers.of(hd))
                .build();

        Long now = System.currentTimeMillis();
        RLock lock = RedisUtil.getLock("JpSankouPdf");
        boolean getLock = false;
        try {
            if (lock.tryLock(0L, 60L, TimeUnit.MILLISECONDS)) {
                getLock = true;
                doPdf(request, now);
            }
        } catch (Exception e) {
            log.error("JpSankouPdf error", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        Long cost = System.currentTimeMillis() - now;
        log.info("JpSankouPdf parse finish, cost: {}ms", cost);
        if (getLock && cost < INTERVAL) {
            try {
                TimeUnit.MILLISECONDS.sleep(INTERVAL - cost);
            } catch (InterruptedException e) {}
        }
    }

    private static void doPdf(Request request, Long start) throws IOException {
        Response response = httpClient.newCall(request).execute();
        if (response.code() == 404) {
            log.info("JpSankou pdf not found");
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            File file = File.createTempFile("sample", ".pdf");
            try (InputStream inputStream = body.byteStream();
                 OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                log.info("JpSankouPdf download finish, cost: {}ms", System.currentTimeMillis() - start);
            }

            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper textStripper = new PDFTextStripper();
                String content = textStripper.getText(document);
                if (content.contains("追 加 参 考 表")) {
                    int index = content.indexOf("追 加 参 考 表");
                    String kw = content.substring(index+12, index + 23);
                    log.info("JpSankouPdf parse, keyword: {}", kw);
                }
            } catch (IOException e) {

            }
        }
    }

    private static void news() {
        Map<String, String> hd = new HashMap<>();

        Request request = new Request.Builder()
                .url(HTML_URL)
                .headers(Headers.of(hd))
                .build();
        Long now = System.currentTimeMillis();
        RLock lock = RedisUtil.getLock("JpSankouNews");
        boolean getLock = false;
        try {
            if (lock.tryLock(0L, 60L, TimeUnit.MILLISECONDS)) {
                getLock = true;
                doNews(request, now);
            }
        } catch (Exception e) {
            log.error("JpSankouNews error", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        Long cost = System.currentTimeMillis() - now;
        log.info("JpSankouNews parse finish, cost: {}ms", cost);
        if (getLock && cost < INTERVAL) {
            try {
                TimeUnit.MILLISECONDS.sleep(INTERVAL - cost);
            } catch (InterruptedException e) {}
        }
    }

    private static void doNews(Request request, Long start) throws IOException {
        Response response = httpClient.newCall(request).execute();
        String body = response.body().string();
        log.info("JpSankouNews download finish, cost: {}ms", System.currentTimeMillis() - start);
        Document document = Jsoup.parse(body, HTML_URL);
        Elements list = document.body().select("#section");
        String keyword = list.get(0).select("article").get(1).select("p").get(0).text();
        log.info("JpSankouNews parse, keyword: {}", keyword);
    }
}
