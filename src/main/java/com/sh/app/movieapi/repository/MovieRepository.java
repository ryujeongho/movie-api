package com.sh.app.movieapi.repository;

import com.sh.app.movieapi.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
