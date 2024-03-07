package com.sh.app.movieapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sh.app.movieapi.entity.Movie;
import com.sh.app.movieapi.entity.MovieResponse;
import com.sh.app.movieapi.genre.entity.Genre;
import com.sh.app.movieapi.genre.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.sh.app.movieapi.repository.MovieRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Transactional
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;

    public void saveMoviesToDatabase() {
        List<Movie> movies = fetchMovies();
        for (Movie movie : movies) {
            // 데이터베이스에 영화가 이미 존재하는지 확인
            Optional<Movie> existingMovie = movieRepository.findById(movie.getId());
            if (!existingMovie.isPresent()) {
                movieRepository.save(movie); // 영화가 존재하지 않는 경우, 저장
            } else {
                // 존재하는 경우, 필요한 업데이트 로직을 추가
                Movie updateMovie = existingMovie.get();
                updateMovie.setTitle(movie.getTitle());
                // 필요한 다른 필드 업데이트
                movieRepository.save(updateMovie); // 업데이트된 엔티티 저장
            }
        }
    }

    @Value("${api_tmdb_key}")
    private String tmdbApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String BASE_URL = "https://api.themoviedb.org/3/movie/now_playing";

    public List<Movie> fetchMovies() {
        List<Movie> allMovies = new ArrayList<>();
        for (int page = 1; page <= 4; page++) {
            String url = String.format("%s?api_key=%s&include_adult=false&include_video=true&language=ko-KR&page=%d&region=KR&sort_by=popularity.desc", BASE_URL, tmdbApiKey, page);
            String response = restTemplate.getForObject(url, String.class);
            try {
                MovieResponse movieResponse = objectMapper.readValue(response, MovieResponse.class);
                allMovies.addAll(movieResponse.getResults());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return allMovies;
    }

    public void saveMovie(Movie movie, List<Long> genreIds) {
        Set<Genre> genres = new HashSet<>();
        for (Long genreId : genreIds) {
            Genre genre = genreRepository.findById(genreId)
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setId(genreId);
                        return genreRepository.save(newGenre);
                    });
            genres.add(genre);
        }
        movie.setGenres(genres);
        movieRepository.save(movie);
    }
}
