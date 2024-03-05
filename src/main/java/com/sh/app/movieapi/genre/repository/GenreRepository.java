package com.sh.app.movieapi.genre.repository;

import com.sh.app.movieapi.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
