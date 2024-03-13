package com.sh.app.movieapi.tgenre.repository;

import com.sh.app.movieapi.tgenre.entity.Tgenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TgenreRepository extends JpaRepository<Tgenre, Long> {
}
