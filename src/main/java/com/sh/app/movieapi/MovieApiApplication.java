package com.sh.app.movieapi;

import com.sh.app.movieapi.boxoffice.service.BoxOfficeService;
import com.sh.app.movieapi.genre.service.GenreService;
import com.sh.app.movieapi.service.MovieService;
import com.sh.app.movieapi.service.TmdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling
public class MovieApiApplication implements CommandLineRunner {

	@Autowired
	private MovieService movieService;

	@Autowired
	private GenreService genreService;

	@Autowired
	private BoxOfficeService boxOfficeService;

	@Autowired
	private TmdbService tmdbService;

	public static void main(String[] args) {
		SpringApplication.run(MovieApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
//		movieService.saveMoviesToDatabase(); // 데이터베이스에 영화 데이터 저장
//		genreService.fetchAndSaveGenres();
//		boxOfficeService.fetchAndStoreBoxOfficeData();
		tmdbService.fetchAndStoreMovieData();
	}

}
