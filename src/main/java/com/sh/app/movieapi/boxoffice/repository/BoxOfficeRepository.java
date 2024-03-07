package com.sh.app.movieapi.boxoffice.repository;

import com.sh.app.movieapi.boxoffice.entity.BoxOffice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoxOfficeRepository extends JpaRepository<BoxOffice, Long> {
}
