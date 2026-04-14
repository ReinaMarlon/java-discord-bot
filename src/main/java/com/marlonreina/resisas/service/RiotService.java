package com.marlonreina.resisas.service;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class RiotService {

    private static final String henrydevKey = System.getenv("HENRY_API_TOKEN");

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    private HttpResponse<String> get(String url, String authKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authKey)
                .timeout(java.time.Duration.ofSeconds(15)) // timeout por request
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String getValorantRank(String region, String name, String tag) throws Exception {
        String encodedName = java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        String encodedTag = java.net.URLEncoder.encode(tag, "UTF-8").replace("+", "%20");
        return get("https://api.henrikdev.xyz/valorant/v1/mmr/"
                + region
                + "/"
                + encodedName
                + "/"
                + encodedTag, henrydevKey).body();
    }

    public String getMmR(String region, String name, String tag) throws Exception {
        String encodedName = java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        String encodedTag = java.net.URLEncoder.encode(tag, "UTF-8").replace("+", "%20");
        return get("https://api.henrikdev.xyz/valorant/v2/mmr/"
                + region
                + "/"
                + encodedName
                + "/"
                + encodedTag, henrydevKey).body();
    }

    public String getMatches(String region, String name, String tag) throws Exception {
        String encodedName = java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        String encodedTag = java.net.URLEncoder.encode(tag, "UTF-8").replace("+", "%20");
        return get("https://api.henrikdev.xyz/valorant/v3/matches/"
                + region
                + "/"
                + encodedName
                + "/"
                + encodedTag, henrydevKey).body();
    }

    public String getMatchHistory(String region, String name, String tag, int size) throws Exception {
        String encodedName = java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        String encodedTag = java.net.URLEncoder.encode(tag, "UTF-8").replace("+", "%20");
        return get("https://api.henrikdev.xyz/valorant/v3/matches/"
                + region
                + "/"
                + encodedName
                + "/"
                + encodedTag
                + "?mode=competitive&size="
                + size, henrydevKey).body();
    }

    public String getMmRhistory(String region, String name, String tag) throws Exception {
        String encodedName = java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20");
        String encodedTag = java.net.URLEncoder.encode(tag, "UTF-8").replace("+", "%20");
        return get("https://api.henrikdev.xyz/valorant/v1/mmr-history/"
                + region
                + "/"
                + encodedName
                + "/"
                + encodedTag, henrydevKey).body();
    }

}