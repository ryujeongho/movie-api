package com.sh.app.movieapi.tmovievideo.repository;

import com.sh.app.movieapi.tmovievideo.entity.TmovieVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmovieVideoRepository extends JpaRepository<TmovieVideo, Long> {
}
