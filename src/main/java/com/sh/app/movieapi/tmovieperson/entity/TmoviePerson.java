package com.sh.app.movieapi.tmovieperson.entity;

import com.sh.app.movieapi.tmovie.entity.Tmovie;
import com.sh.app.movieapi.tperson.entity.Tperson;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmoviePerson {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Tmovie movie;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Tperson person;

    private String role; // 영화 내에서의 역할 (예: "Actor", "Director")
}
