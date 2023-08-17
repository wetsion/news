package org.example;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UnemploymentPdfRandom {

    protected static OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final static String URL = "https://www.dol.gov/sites/dolgov/files/OPA/newsreleases/ui-claims/2023";

    private final static Long INIT = 1764L;

    private final static Long INTERVAL = 600L;

    private final static String SERIAL_KEY = "UnemploymentPdfRandomSerialTmp";


    public static void main(String[] args) {
        String step = System.getProperty("step");
        if (StringUtils.isNotBlank(step)) {
            doDownLoad(Long.parseLong(step));
        } else {
            doDownLoad(0L);
        }
    }

    private static void doDownLoad(Long step) {
        Map<String, String> hd = new HashMap<>();
        hd.put("authority", "www.dol.gov");
        hd.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        hd.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7");
        hd.put("cache-control", "no-cache");
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

        Long currentStep = INIT + step;
        Request request = new Request.Builder()
                .url(URL + currentStep + ".pdf")
                .headers(Headers.of(hd))
                .build();
        while (true) {
            Long now = System.currentTimeMillis();
            try {
                doCrawler(request, now, currentStep);
            } catch (Exception e) {
                log.error("UnemploymentPdfRandom step: {} error", currentStep, e);
            }
            Long cost = System.currentTimeMillis() - now;
            log.info("UnemploymentPdfRandom step: {} parse finish, cost: {}ms", currentStep, cost);
            if (cost < INTERVAL) {
                try {
                    TimeUnit.MILLISECONDS.sleep(INTERVAL - cost);
                } catch (InterruptedException e) {}
            }
        }
    }

    private static void doCrawler(Request request, Long start, Long currentStep) throws IOException {
        Response response = httpClient.newCall(request).execute();
        if (response.code() == 404) {
            response.close();
            log.info("UnemploymentPdfRandom not found, step: {} cost: {}ms", currentStep, System.currentTimeMillis() - start);
            return;
        } else if (response.code() != 200) {
            response.close();
            log.info("UnemploymentPdfRandom not success, step: {} cost: {}ms", currentStep, System.currentTimeMillis() - start);
            return;
        }
        log.info("UnemploymentPdfRandom found! step: {} cost: {}ms", currentStep, System.currentTimeMillis() - start);
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
                log.info("UnemploymentPdfRandom download finish, step: {} cost: {}ms", currentStep, System.currentTimeMillis() - start);
            }

            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper textStripper = new PDFTextStripper();
                String content = textStripper.getText(document);
                if (content.contains("TRANSMISSION OF MATERIALS IN THIS RELEASE IS EMBARGOED UNTIL")) {
                    int index = content.indexOf("TRANSMISSION OF MATERIALS IN THIS RELEASE IS EMBARGOED UNTIL");
                    String kw = content.substring(index+62, index+100);
                    log.info("UnemploymentPdfRandom parse, step: {} keyword: {}", currentStep, kw);
                }
                log.info("UnemploymentPdfRandom not contain, step: {}", currentStep);
            } catch (IOException e) {

            }
        }


    }
}
