package com.sh.app.movieapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sh.app.movieapi.tgenre.entity.Tgenre;
import com.sh.app.movieapi.tgenre.repository.TgenreRepository;
import com.sh.app.movieapi.tmovie.entity.Tmovie;
import com.sh.app.movieapi.tmovie.repository.TmovieRepository;
import com.sh.app.movieapi.tmovieperson.entity.TmoviePerson;
import com.sh.app.movieapi.tmovieperson.repository.TmoviePersonRepository;
import com.sh.app.movieapi.tmovievideo.entity.TmovieVideo;
import com.sh.app.movieapi.tmovievideo.repository.TmovieVideoRepository;
import com.sh.app.movieapi.tperson.entity.Tperson;
import com.sh.app.movieapi.tperson.repository.TpersonRepository;
import com.sh.app.movieapi.tvideo.entity.Tvideo;
import com.sh.app.movieapi.tvideo.repository.TvideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TmdbService {
    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${api_tmdb_key}")
    private String tmdbApiKey;

    @Autowired
    private TgenreRepository tgenreRepository;
    @Autowired
    private TmovieRepository tmovieRepository;
    @Autowired
    private TvideoRepository videoRepository;
    @Autowired
    private TmovieVideoRepository movieVideoRepository;
    @Autowired
    private TpersonRepository personRepository;
    @Autowired
    private TmoviePersonRepository moviePersonRepository;

    private static final String GENRE_URL = "https://api.themoviedb.org/3/genre/movie/list?language=ko-KR&api_key=%s";
    private static final String NOW_PLAYING_URL = "https://api.themoviedb.org/3/movie/now_playing?language=ko-KR&page=1&region=KR&api_key=%s";
    private static final String VIDEO_URL = "https://api.themoviedb.org/3/movie/%d/videos?language=ko-KR&api_key=%s";
    private static final String CREDITS_URL = "https://api.themoviedb.org/3/movie/%d/credits?language=ko-KR&api_key=%s";

    public void fetchAndStoreMovieData() {
        fetchAndStoreGenres();
        fetchAndStoreMovies();

    }

    private void fetchAndStoreGenres() {
        String url = String.format(GENRE_URL, tmdbApiKey);
        String response = restTemplate.getForObject(url, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            for (JsonNode node : root.path("genres")) {
                Tgenre genre = new Tgenre();
                genre.setId(node.get("id").asLong());
                genre.setName(node.get("name").asText());
                tgenreRepository.save(genre);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchAndStoreMovies() {
        String url = String.format(NOW_PLAYING_URL, tmdbApiKey);
        String response = restTemplate.getForObject(url, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            for (JsonNode node : root.path("results")) {
                Tmovie movie = new Tmovie();
                movie.setId(node.get("id").asLong());
                movie.setTitle(node.get("title").asText());
                movie.setReleaseDate(node.get("release_date").asText());

                Set<Tgenre> movieGenres = new HashSet<>();
                for (JsonNode genreId : node.path("genre_ids")) {
                    tgenreRepository.findById(genreId.asLong()).ifPresent(movieGenres::add);
                }
                movie.setGenres(movieGenres);
                tmovieRepository.save(movie);

                fetchAndStoreMovieVideos(movie.getId());
                fetchAndStoreMovieCredits(movie.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fetchAndStoreMovieVideos(Long movieId) {
        String url = String.format(VIDEO_URL, movieId, tmdbApiKey);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            for (JsonNode node : root.path("results")) {
                if ("Trailer".equals(node.get("type").textValue())) {
                    Tvideo video = new Tvideo();
                    video.setVideoId(node.get("id").textValue());
                    video.setType(node.get("type").textValue());
                    video.setName(node.get("name").textValue());
                    video.setKey(node.get("key").textValue());

                    Tmovie movie = tmovieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));
                    video.setMovie(movie);
                    videoRepository.save(video);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void fetchAndStoreMovieCredits(Long movieId) {
        String url = String.format(CREDITS_URL, movieId, tmdbApiKey);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Tmovie movie = tmovieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            processCast(root.path("cast"), movie);
            processCrew(root.path("crew"), movie);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void processCast(JsonNode castNode, Tmovie movie) {
        for (JsonNode node : castNode) {
            int order = node.get("order").asInt();
            if (order <= 4) {
                Tperson actor = new Tperson();
                actor.setName(node.get("name").textValue());
                actor.setRole("Actor");
                Tperson savedActor = personRepository.save(actor);

                TmoviePerson moviePerson = new TmoviePerson();
                moviePerson.setMovie(movie);
                moviePerson.setPerson(savedActor);
                moviePerson.setRole(actor.getRole());
                moviePersonRepository.save(moviePerson);
            }
        }
    }

    private void processCrew(JsonNode crewNode, Tmovie movie) {
        for (JsonNode node : crewNode) {
            if ("Director".equals(node.get("job").textValue())) {
                Tperson director = new Tperson();
                director.setName(node.get("name").textValue());
                director.setRole("Director");
                Tperson savedDirector = personRepository.save(director);

                TmoviePerson moviePerson = new TmoviePerson();
                moviePerson.setMovie(movie);
                moviePerson.setPerson(savedDirector);
                moviePerson.setRole(director.getRole());
                moviePersonRepository.save(moviePerson);
            }
        }
    }

//    public void fetchAndStoreMovieCredits(Long movieId) {
//        String url = String.format(CREDITS_URL, movieId, tmdbApiKey);
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//
//        try {
//            JsonNode root = objectMapper.readTree(response.getBody());
//            JsonNode castNode = root.path("cast");
//            JsonNode crewNode = root.path("crew");
//
//            for (JsonNode node : castNode) {
//                int order = node.get("order").asInt();
//                if (order <= 4) {
//                    Tperson actor = new Tperson();
//                    actor.setName(node.get("name").textValue());
//                    actor.setRole("Actor");
//                    // actor에 대한 추가 설정...
//                    personRepository.save(actor);
//                }
//            }
//
//            for (JsonNode node : crewNode) {
//                if ("Director".equals(node.get("job").textValue())) {
//                    Tperson director = new Tperson();
//                    director.setName(node.get("name").textValue());
//                    director.setRole("Director");
//                    // director에 대한 추가 설정...
//                    personRepository.save(director);
//                }
//            }
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }
}
