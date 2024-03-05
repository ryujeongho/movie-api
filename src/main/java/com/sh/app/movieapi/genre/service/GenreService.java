package com.sh.app.movieapi.genre.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sh.app.movieapi.genre.entity.Genre;
import com.sh.app.movieapi.genre.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    private final String API_KEY = "1ddb82a777a1ed86e400004d9119cdf3";
    private final String API_URL = "https://api.themoviedb.org/3/genre/movie/list?language=ko&api_key=" + API_KEY;

    public void fetchAndSaveGenres() {
        String response = restTemplate.getForObject(API_URL, String.class);
        try {
            GenreResponse genreResponse = objectMapper.readValue(response, GenreResponse.class);
            List<Genre> genres = genreResponse.getGenres();
            genreRepository.saveAll(genres);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class GenreResponse {
        private List<Genre> genres;

        public List<Genre> getGenres() {
            return genres;
        }

        public void setGenres(List<Genre> genres) {
            this.genres = genres;
        }
    }
}
