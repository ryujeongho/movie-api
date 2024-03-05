package com.sh.app.movieapi.genre.entity;

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
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_genre_no_generator")
    @SequenceGenerator(
            name = "seq_genre_no_generator",
            sequenceName = "seq_genre_no",
            initialValue = 1,
            allocationSize = 1
    )
    private Long no;

    private Long id;

    private String name;
}
