package com.sh.app.movieapi.genre.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sh.app.movieapi.genre.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenreResponse {
    private List<GenreDto> genres;
}
