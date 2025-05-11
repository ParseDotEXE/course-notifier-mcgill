package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class TestVSBApi {
    public static void main(String[] args) {
        try {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            
            HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
            
            // First visit the main page
            String mainPageUrl = "https://vsb.mcgill.ca/criteria.jsp?access=0&lang=en&tip=2&page=criteria&scratch=0&advice=0&legend=1&term=202509&sort=none&filters=iiiiiiiiii&bbs=&ds=&cams=DISTANCE_DOWNTOWN_MACDONALD_OFF-CAMPUS&locs=any&isrts=any&ses=any&pl=&pac=1";
            
            HttpRequest mainPageRequest = HttpRequest.newBuilder()
                .uri(URI.create(mainPageUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .GET()
                .build();
                
            client.send(mainPageRequest, HttpResponse.BodyHandlers.ofString());
            
            // Make API call WITHOUT va_0_0 parameter
            String apiUrl = "https://vsb.mcgill.ca/api/class-data?term=202509&course_0_0=COMP-273&rq_0_0=&t=438&e=27&nouser=1&_=" + System.currentTimeMillis();
            
            HttpRequest apiRequest = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/xml, text/xml, */*; q=0.01")
                .header("Referer", mainPageUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("X-Requested-With", "XMLHttpRequest")
                .GET()
                .build();
                
            HttpResponse<String> apiResponse = client.send(apiRequest, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("API Status: " + apiResponse.statusCode());
            System.out.println("API Response: " + apiResponse.body());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}