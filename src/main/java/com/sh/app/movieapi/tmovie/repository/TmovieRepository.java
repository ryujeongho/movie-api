package com.sh.app.movieapi.tmovie.repository;

import com.sh.app.movieapi.tmovie.entity.Tmovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmovieRepository extends JpaRepository<Tmovie, Long> {
}
