package com.sh.app.movieapi.tmovie.entity;

import com.sh.app.movieapi.tgenre.entity.Tgenre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tmovie {
    @Id
    private Long id;
    private String title;
    private String releaseDate;

    @ManyToMany
    @JoinTable(
            name = "tmovie_tgenre",
            joinColumns = @JoinColumn(name = "tmovie_id"),
            inverseJoinColumns = @JoinColumn(name = "tgenre_id")
    )
    private Set<Tgenre> genres = new HashSet<>();
}
