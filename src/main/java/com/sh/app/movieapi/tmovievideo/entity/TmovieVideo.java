package com.sh.app.movieapi.tmovievideo.entity;

import com.sh.app.movieapi.tmovie.entity.Tmovie;
import com.sh.app.movieapi.tvideo.entity.Tvideo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Mutability;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmovieVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Tmovie movie;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Tvideo video;
}
