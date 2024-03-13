package com.sh.app.movieapi.tperson.repository;

import com.sh.app.movieapi.tperson.entity.Tperson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TpersonRepository extends JpaRepository<Tperson, Long> {
}
