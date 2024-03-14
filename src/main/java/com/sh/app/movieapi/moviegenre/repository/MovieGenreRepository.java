package com.sh.app.movieapi.moviegenre.repository;

import com.sh.app.movieapi.moviegenre.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
}
