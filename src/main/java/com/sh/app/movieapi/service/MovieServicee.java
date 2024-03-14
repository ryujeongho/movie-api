//package com.sh.app.movieapi.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sh.app.movieapi.entity.Movie;
//import com.sh.app.movieapi.entity.MovieDetailsResponse;
//import com.sh.app.movieapi.entity.MovieResponse;
//import com.sh.app.movieapi.genre.entity.Genre;
//import com.sh.app.movieapi.genre.repository.GenreRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import com.sh.app.movieapi.repository.MovieeRepository;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.util.*;
//
//@Service
//@Transactional
//public class MovieServicee {
//
//    @Autowired
//    private MovieeRepository movieRepository;
//    @Autowired
//    private GenreRepository genreRepository;
//
//    public void saveMoviesToDatabase() {
//        List<Movie> movies = fetchMovies();
//        for (Movie movie : movies) {
//            // 데이터베이스에 영화가 이미 존재하는지 확인
//            Optional<Movie> existingMovie = movieRepository.findById(movie.getId());
//            if (!existingMovie.isPresent()) {
//                movieRepository.save(movie); // 영화가 존재하지 않는 경우, 저장
//            } else {
//                // 존재하는 경우, 필요한 업데이트 로직을 추가
//                Movie updateMovie = existingMovie.get();
//                updateMovie.setTitle(movie.getTitle());
//                // 필요한 다른 필드 업데이트
//                movieRepository.save(updateMovie); // 업데이트된 엔티티 저장
//            }
//        }
//    }
//
//    private RestTemplate restTemplate = new RestTemplate();
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    @Value("${api_tmdb_key}")
//    private String tmdbApiKey; // TMDb API 키는 application.properties에서 가져옵니다.
//
//    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/now_playing";
//    private static final String DETAIL_URL = "https://api.themoviedb.org/3/movie/%d?language=en-US&api_key=%s";
//
//    public List<Movie> fetchMovies() {
//        List<Movie> allMovies = new ArrayList<>();
//        for (int page = 1; page <= 4; page++) {
//            UriComponentsBuilder uriBuilder = UriComponentsBuilder
//                    .fromHttpUrl(BASE_URL)
//                    .queryParam("api_key", tmdbApiKey)
//                    .queryParam("language", "ko-KR")
//                    .queryParam("page", page)
//                    .queryParam("region", "KR");
//            String url = uriBuilder.toUriString();
//
//            String response = restTemplate.getForObject(url, String.class);
//            try {
//                // API 응답을 MovieResponse DTO로 변환합니다.
//                MovieResponse movieResponse = objectMapper.readValue(response, MovieResponse.class);
//                allMovies.addAll(movieResponse.getResults());
//
//                // 이제 각 영화의 상세 정보를 가져와서 저장합니다.
//                for (Movie movie : movieResponse.getResults()) {
//                    String movieDetailsUrl = String.format(DETAIL_URL, movie.getId(), tmdbApiKey);
//                    String movieDetailsResponse = restTemplate.getForObject(movieDetailsUrl, String.class);
//
//                    // API 응답을 MovieDetailsResponse DTO로 변환합니다.
//                    MovieDetailsResponse movieDetails = objectMapper.readValue(movieDetailsResponse, MovieDetailsResponse.class);
//
//                    // 영화 상세 정보를 Movie 엔티티에 추가합니다.
//                    movie.setTitle(movieDetails.getTitle());
//                    movie.setOverview(movieDetails.getOverview());
//                    movie.setRelease_date(movieDetails.getReleaseDate());
//                    movie.setStatus(movieDetails.getStatus());
//                    // 다른 필요한 정보도 이와 같은 방식으로 설정합니다.
//
//                    // 변경된 Movie 객체를 저장소에 저장합니다.
//                    // movieRepository.save(movie); // 적절한 JPA 리포지토리를 사용하세요.
//                }
//            } catch (Exception e) {
//                e.printStackTrace(); // 예외 처리는 로그로 남기거나 적절하게 처리합니다.
//            }
//        }
//        return allMovies;
//    }
//
//    public void saveMovie(Movie movie, List<Long> genreIds) {
//        Set<Genre> genres = new HashSet<>();
//        for (Long genreId : genreIds) {
//            Genre genre = genreRepository.findById(genreId)
//                    .orElseGet(() -> {
//                        Genre newGenre = new Genre();
//                        newGenre.setId(genreId);
//                        return genreRepository.save(newGenre);
//                    });
//            genres.add(genre);
//        }
//        movie.setGenres(genres);
//        movieRepository.save(movie);
//    }
//}
