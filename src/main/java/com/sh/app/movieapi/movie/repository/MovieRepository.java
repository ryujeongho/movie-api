package com.sh.app.movieapi.movie.repository;

import com.sh.app.movieapi.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

}
