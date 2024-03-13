package com.sh.app.movieapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sh.app.movieapi.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_movie_no_generator")
    @SequenceGenerator(
            name = "seq_movie_no_generator",
            sequenceName = "seq_movie_no",
            initialValue = 1,
            allocationSize = 1
    )
    private Long no;

    private Long id;

    private String poster_path;

    private Double vote_average;

    private String release_date;

    @Column(length = 2000)
    private String overview;

    private String title;

    private String status;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

}
