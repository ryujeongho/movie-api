package com.sh.app.movieapi.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sh.app.movieapi.genre.dto.GenreDto;
import com.sh.app.movieapi.genre.dto.GenreResponse;
import com.sh.app.movieapi.genre.entity.Genre;
import com.sh.app.movieapi.genre.repository.GenreRepository;
import com.sh.app.movieapi.movie.dto.*;
import com.sh.app.movieapi.movie.entity.Movie;
import com.sh.app.movieapi.movie.repository.MovieRepository;
import com.sh.app.movieapi.moviegenre.entity.MovieGenre;
import com.sh.app.movieapi.moviegenre.repository.MovieGenreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class MovieService {
    @Value("${api_kmdb_key}")
    private String kmdbApiKey;

    @Value("${api_tmdb_key}")
    private String tmdbApiKey;

    private ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RestTemplate restTemplateCustom;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    private static final String NOW_PLAYING_URL = "https://api.themoviedb.org/3/movie/now_playing";

    private static final String INFO_URL = "https://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp";

    private static final String GENRE_URL = "https://api.themoviedb.org/3/genre/movie/list";

    public void fetchAndStoreMovieData() {
        fetchAndStoreGenres();
        fetchAndStoreAllMovies();
    }

    private void fetchAndStoreGenres() {
        String url = UriComponentsBuilder
                .fromHttpUrl(GENRE_URL)
                .queryParam("api_key", tmdbApiKey)
                .queryParam("language", "ko-KR")
                .toUriString();

        GenreResponse response = restTemplate.getForObject(url, GenreResponse.class);
        if (response != null) {
            try {
                for (GenreDto genreDto : response.getGenres()) {
                    Genre genre = convertToGenre(genreDto);

                    genreRepository.save(genre);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Genre convertToGenre(GenreDto genreDto) {
        Genre genre = modelMapper.map(genreDto, Genre.class);
        return genre;
    }

    public void fetchAndStoreAllMovies() {
        int totalPages = fetchTotalPages();
        for (int page = 1; page <= totalPages; page++) {
            fetchAndStoreMoviesByPage(page);
        }
    }

    private int fetchTotalPages() {
        String url = buildUrl(1); // 첫 번째 페이지 URL 생성
        MovieResponse
                response = restTemplate.getForObject(url, MovieResponse.class);
        return response != null ? response.getTotalPages() : 0;
    }

    private void fetchAndStoreMoviesByPage(int page) {
        String url = buildUrl(page);
        MovieResponse response = restTemplate.getForObject(url, MovieResponse.class);
        if (response != null) {
            for (MovieDto movieDto : response.getResults()) {
                Movie movie = convertToMovie(movieDto);

                String formattedReleaseDate = movie.getReleaseDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                MovieInfoDto movieInfoDto = fetchKmdbMovieInfo(movie.getTitle(), formattedReleaseDate);

                // movieInfoDto가 null이 아닌 경우 각 필드 검사
                if (movieInfoDto != null) {
                    // rating 필드 검사: null이면 "정보 없음"을, 아니면 해당 값을 사용
                    if (movieInfoDto.getRating() != null) {
                        movie.setFilmRatings(movieInfoDto.getRating());
                    } else {
                        movie.setFilmRatings("정보 없음");
                    }
                    // runtime 필드 검사: null이면 0을, 아니면 해당 값을 사용
                    if (movieInfoDto.getRuntime() != null) {
                        movie.setRuntime(movieInfoDto.getRuntime());
                    } else {
                        movie.setRuntime(0);
                    }
                } else {
                    // movieInfoDto 자체가 null인 경우 기본값 설정
                    movie.setFilmRatings("정보 없음");
                    movie.setRuntime(0);
                }


                Movie saveMovie = movieRepository.save(movie); // 영화 정보 저장

                // 장르 ID 리스트를 반복하면서 MovieGenre 인스턴스를 생성하고 저장
                for (Long genreId : movieDto.getGenreIds()) {
                    // 각 장르 ID에 해당하는 Genre 엔티티를 찾음
                    Genre genre = genreRepository.findById(genreId)
                            .orElseThrow(() -> new EntityNotFoundException("Genre not found for ID: " + genreId));

                    // MovieGenre 인스턴스 생성 및 Movie와 Genre 연결
                    MovieGenre movieGenre = MovieGenre.builder()
                            .movie(saveMovie)
                            .genre(genre)
                            .build();

                    // MovieGenre 인스턴스 저장
                    movieGenreRepository.save(movieGenre);
                }
            }
        }
    }

    private Movie convertToMovie(MovieDto movieDto) {
        Movie movie = modelMapper.map(movieDto, Movie.class);
        return movie;
    }

    private String buildUrl(int page) {
        return UriComponentsBuilder
                .fromHttpUrl(NOW_PLAYING_URL)
                .queryParam("api_key", tmdbApiKey)
                .queryParam("language", "ko-KR")
                .queryParam("page", page)
                .queryParam("region", "KR")
                .toUriString();
    }

    private URI buildKmdbUrl(String title, String formattedReleaseDate) {
        return UriComponentsBuilder
                .fromHttpUrl(INFO_URL)
                .queryParam("collection", "kmdb_new2")
                .queryParam("detail", "Y")
                .queryParam("title", title)
                .queryParam("releaseDts", formattedReleaseDate)
                .queryParam("ServiceKey", kmdbApiKey)
                .build()
                .encode()
                .toUri();
    }

    private MovieInfoDto fetchKmdbMovieInfo(String title, String formattedReleaseDate) {
        URI kmdbUrl = buildKmdbUrl(title, formattedReleaseDate);
        KmdbResponse kmdbResponse = restTemplateCustom.getForObject(kmdbUrl, KmdbResponse.class);
        log.debug("kmdbResponse = {}", kmdbResponse);
        if (kmdbResponse != null && kmdbResponse.getData() != null) {
            for (Result result : kmdbResponse.getData()) {
                if (result.getResult() != null) {
                    for (MovieInfoDto movieInfoDto : result.getResult()) {
                        return movieInfoDto;
                    }
                }
            }
        }
        return null;
    }

//    private MovieInfoDto fetchKmdbMovieInfo(String title, String formattedReleaseDate) {
//        URI kmdbUrl = buildKmdbUrl(title, formattedReleaseDate);
//        String kmdbResponse = restTemplate.getForObject(kmdbUrl, String.class);
//        log.debug("kmdbResponse = {}", kmdbResponse);
//        if (kmdbResponse != null) {
//            JSONObject jsonResponse = new JSONObject(kmdbResponse);
//            JSONArray data = jsonResponse.optJSONArray("Data");
//            if (data == null || data.isEmpty()) {
//                // 데이터 배열이 비어있거나 없는 경우의 처리
//                return new MovieInfoDto("데이터 없음", 0);
//            }
//
//            JSONObject movie = data.optJSONObject(0);
//            if (movie == null) {
//                // 첫 번째 영화 객체가 없는 경우의 처리
//                return new MovieInfoDto("데이터 없음", 0);
//            }
//
//            JSONArray result = movie.optJSONArray("Result");
//            if (result == null || result.isEmpty()) {
//                // 결과 배열이 비어있거나 없는 경우의 처리
//                return new MovieInfoDto("데이터 없음", 0);
//            }
//
//            JSONObject firstMovie = result.optJSONObject(0);
//            if (firstMovie == null) {
//                // 첫 번째 결과 객체가 없는 경우의 처리
//                return new MovieInfoDto("데이터 없음", 0);
//            }
//            if (firstMovie != null) {
//                // 런타임
//                Integer runtime = firstMovie.optInt("runtime", 0);
//
//                // 관람 등급
//                String rating = firstMovie.optString("rating", "정보 없음");
//
//                return new MovieInfoDto(rating, runtime);
//            }
//        }
//        return null;
//    }

//    private MovieInfoDto fetchKmdbMovieInfo(String title, String formattedReleaseDate) {
//        URI kmdbUrl = buildKmdbUrl(title, formattedReleaseDate);
//        String kmdbResponse = restTemplate.getForObject(kmdbUrl, String.class);
//        return parseKmdbResponse(kmdbResponse);
//    }

//    private MovieInfoDto parseKmdbResponse(KmdbResponse kmdbResponse) {
//        JSONObject jsonResponse = new JSONObject(kmdbResponse);
//        JSONArray data = jsonResponse.optJSONArray("Data");
//        if (data == null || data.isEmpty()) {
//            // 데이터 배열이 비어있거나 없는 경우의 처리
//            return new MovieInfoDto("데이터 없음", 0);
//        }
//
//        JSONObject movie = data.optJSONObject(0);
//        if (movie == null) {
//            // 첫 번째 영화 객체가 없는 경우의 처리
//            return new MovieInfoDto("데이터 없음", 0);
//        }
//
//        JSONArray result = movie.optJSONArray("Result");
//        if (result == null || result.isEmpty()) {
//            // 결과 배열이 비어있거나 없는 경우의 처리
//            return new MovieInfoDto("데이터 없음", 0);
//        }
//
//        JSONObject firstMovie = result.optJSONObject(0);
//        if (firstMovie == null) {
//            // 첫 번째 결과 객체가 없는 경우의 처리
//            return new MovieInfoDto("데이터 없음", 0);
//        }
//        if (firstMovie != null) {
//            // 런타임
//            Integer runtime = firstMovie.optInt("runtime", 0);
//
//            // 관람 등급
//            String rating = firstMovie.optString("rating", "정보 없음");
//
//            return new MovieInfoDto(rating, runtime);
//        }
//        return null;
//    }

//    private void fetchAndStoreGenres() {
//        String url = UriComponentsBuilder
//                .fromHttpUrl(GENRE_URL)
//                .queryParam("api_key", tmdbApiKey)
//                .queryParam("language", "ko-KR")
//                .toUriString();
//
//        String response = restTemplate.getForObject(url, String.class);
//        try {
//            JsonNode root = objectMapper.readTree(response);
//            for (JsonNode node : root.path("genres")) {
//                Genre genre = new Genre();
//                genre.setId(node.get("id").asLong());
//                genre.setName(node.get("name").asText());
//                genreRepository.save(genre);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
