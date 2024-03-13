package com.sh.app.movieapi.tmovieperson.repository;

import com.sh.app.movieapi.tmovieperson.entity.TmoviePerson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmoviePersonRepository extends JpaRepository<TmoviePerson, Long> {
}
