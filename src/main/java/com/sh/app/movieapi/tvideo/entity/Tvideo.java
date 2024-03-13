package com.sh.app.movieapi.tvideo.entity;

import com.sh.app.movieapi.tmovie.entity.Tmovie;
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
public class Tvideo {
    @Id
    private String videoId;
    private String type;
    private String name;
    private String key;

    // ManyToOne relation with Movie
    @ManyToOne
    @JoinColumn(name = "tmovie_id")
    private Tmovie movie;
}
