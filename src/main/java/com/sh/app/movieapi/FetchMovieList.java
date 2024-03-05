package com.sh.app.movieapi;

import org.springframework.web.client.RestTemplate;

public class FetchMovieList {

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://api.themoviedb.org/3/movie/now_playing";
        String apiKey = "1ddb82a777a1ed86e400004d9119cdf3"; // API 키를 여기에 입력하세요.
        String parameters = String.format("?api_key=%s&include_adult=false&include_video=true&language=ko-KR&region=KR&sort_by=popularity.desc", apiKey);

        for (int page = 1; page <= 4; page++) {
            String url = baseUrl + parameters + "&page=" + page;
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Page " + page + " Response: " + response);
        }
    }
}