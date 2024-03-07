package com.sh.app.movieapi.boxoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieInfoDto {
    private String director;
    private String actors;
    private String rating;
    private String posterUrl;
    private String vodUrl;
    private Long runtime;
    private String plotText;
}
