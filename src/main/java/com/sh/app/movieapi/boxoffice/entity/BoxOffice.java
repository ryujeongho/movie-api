package com.sh.app.movieapi.boxoffice.entity;

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
public class BoxOffice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_box_office_id_generator")
    @SequenceGenerator(
            name = "seq_box_office_id_generator",
            sequenceName = "seq_box_office_id",
            initialValue = 1,
            allocationSize = 1
    )
    private Long id;

    private Long rank;

    private String movieNm;

    private String openDt;

    private Long audiAcc;

    private String director;

    private String actors;

    private String rating;

    private String posterUrl;

    private String vodUrl;

    private Long runtime;

    @Column(length = 4000)
    private String plotText;
}
