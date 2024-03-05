package com.sh.app.movieapi;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public class WebClientExample {
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIxZGRiODJhNzc3YTFlZDg2ZTQwMDAwNGQ5MTE5Y2RmMyIsInN1YiI6IjY1YjIxZjA1YjFmNjhkMDE2M2Q1MDE3NCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.fE_pOnaSzgXhPlLNF2H_4H5YiNFUZnEZRZ577dWIFgc";

    public static void main(String[] args) {
        WebClient webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + TOKEN)
                .defaultHeader("accept", "application/json")
                .build();

        Mono<String> responseMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/now_playing")
                        .queryParam("include_adult", "false")
                        .queryParam("include_video", "true")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", "1")
                        .queryParam("region", "KR")
                        .queryParam("sort_by", "popularity.desc")
                        .build())
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block(); // 결과가 도착할 때까지 대기
        System.out.println(response);
        responseMono.subscribe(System.out::println); // 비동기적으로 결과 출력

    }
}
