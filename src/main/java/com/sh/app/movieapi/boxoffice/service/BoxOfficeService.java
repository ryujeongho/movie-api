package com.sh.app.movieapi.boxoffice.service;

import com.sh.app.movieapi.boxoffice.dto.MovieInfoDto;
import com.sh.app.movieapi.boxoffice.entity.BoxOffice;
import com.sh.app.movieapi.boxoffice.repository.BoxOfficeRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@Slf4j
public class BoxOfficeService {
    @Autowired
    private BoxOfficeRepository boxOfficeRepository;

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${api_kobis_key}")
    private String kobisApiKey;

    @Value("${api_kmdb_key}")
    private String kmdbApiKey;

    @Value("${api_youtube_key}")
    private String youtubeApiKey;

    public void fetchAndStoreBoxOfficeData() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = yesterday.format(formatter);

        String url = "https://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=" + kobisApiKey + "&targetDt=" + formattedDate;

        String response = restTemplate.getForObject(url, String.class);

        JSONObject jsonObject = new JSONObject(response);
        JSONObject boxOfficeResult = jsonObject.getJSONObject("boxOfficeResult");
        JSONArray dailyBoxOfficeList = boxOfficeResult.getJSONArray("dailyBoxOfficeList");

        log.debug("dailyBoxOfficeList = {}", dailyBoxOfficeList);

        for (int i = 0; i < dailyBoxOfficeList.length(); i++) {
            JSONObject boxOfficeItem = dailyBoxOfficeList.getJSONObject(i);
            BoxOffice boxOffice = new BoxOffice();
            String movieTitle = boxOfficeItem.getString("movieNm");

            String releaseDateStr = boxOfficeItem.getString("openDt");
            LocalDate releaseDate = LocalDate.parse(releaseDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String formattedReleaseDate = releaseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            boxOffice.setMovieNm(movieTitle);
            boxOffice.setRank(boxOfficeItem.getLong("rank"));
            boxOffice.setOpenDt(releaseDate);
            boxOffice.setAudiAcc(boxOfficeItem.getLong("audiAcc"));

            // KMDB에서 영화 정보 가져오기
            MovieInfoDto movieInfoDto = fetchMovieInfoFromKmdb(movieTitle, formattedReleaseDate);

            // BoxOffice 엔티티에 저장
            boxOffice.setDirector(movieInfoDto.getDirector());
            boxOffice.setActors(movieInfoDto.getActors());
            boxOffice.setRating(movieInfoDto.getRating());
            boxOffice.setPosterUrl(movieInfoDto.getPosterUrl());
            boxOffice.setVodUrl(movieInfoDto.getVodUrl());
            boxOffice.setRuntime(movieInfoDto.getRuntime());
            boxOffice.setPlotText(movieInfoDto.getPlotText());

            boxOfficeRepository.save(boxOffice);
        }
    }

    private MovieInfoDto fetchMovieInfoFromKmdb(String movieTitle, String formattedReleaseDate) {
        URI kmdbUrl = UriComponentsBuilder.fromHttpUrl("https://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp")
                .queryParam("collection", "kmdb_new2")
                .queryParam("detail", "Y")
                .queryParam("title", movieTitle)
                .queryParam("releaseDts", formattedReleaseDate)
                .queryParam("ServiceKey", kmdbApiKey)
                .build()
                .encode()
                .toUri();

        String kmdbResponse = restTemplate.getForObject(kmdbUrl, String.class);

        // JSON 응답 파싱
        JSONObject jsonResponse = new JSONObject(kmdbResponse);
        JSONArray data = jsonResponse.optJSONArray("Data");
        JSONObject movie = data.optJSONObject(0);
        JSONArray result = movie.optJSONArray("Result");
        JSONObject firstMovie = result.optJSONObject(0);
        if (firstMovie != null) {
            // directors는 JSONObject입니다. JSONArray가 아닙니다.
            JSONObject directorsObj = firstMovie.optJSONObject("directors");
            String director = "";
            if (directorsObj != null) {
                JSONArray directorsArray = directorsObj.optJSONArray("director");
                // directorsArray가 존재하고, 안에 요소가 있다면 첫 번째 감독의 이름을 가져옵니다.
                if (directorsArray != null && directorsArray.length() > 0) {
                    JSONObject firstDirector = directorsArray.optJSONObject(0);
                    director = firstDirector.optString("directorNm", "정보 없음");
                }
            }

            // 배우 정보 추출 (JSON 구조상 actors는 배열이므로 배열 처리가 필요합니다)
            JSONObject actorsObj = firstMovie.optJSONObject("actors");
            JSONArray actorsArray = actorsObj != null ? actorsObj.optJSONArray("actor") : null;

            StringBuilder actorNames = new StringBuilder();
            if (actorsArray != null) {
                for (int i = 0; i < actorsArray.length() && i < 4; i++) { // 최대 4명까지 반복
                    if (i > 0) {
                        actorNames.append(", ");
                    }
                    actorNames.append(actorsArray.getJSONObject(i).optString("actorNm", "정보 없음"));
                }
            }

            // 포스터 URL 추출
            String postersStr = firstMovie.optString("posters", "");
            String[] postersUrls = postersStr.split("\\|");
            String firstPosterUrl = postersUrls.length > 0 ? postersUrls[0] : "";

            // VOD URL 추출 및 수정
            JSONObject vodsObject = firstMovie.optJSONObject("vods");
            JSONArray vodArray = vodsObject != null ? vodsObject.optJSONArray("vod") : null;
            String modifiedVodUrl = "";
            if (vodArray != null && vodArray.length() > 0) {
                JSONObject firstVod = vodArray.optJSONObject(0);
                String vodUrl = firstVod != null ? firstVod.optString("vodUrl", "") : "";
                if (vodUrl == null || vodUrl.isEmpty()) {
                    // YouTube에서 vodUrl을 검색하여 가져옵니다.
                    vodUrl = fetchVodUrlFromYoutube("영화 " + movieTitle + " 예고편");
                }
                if (!vodUrl.isEmpty()) {
                    // "trailerPlayPop?pFileNm=" 부분을 "play/"로 변경
                    modifiedVodUrl = vodUrl.replace("trailerPlayPop?pFileNm=", "play/");
                }
            }

            // 런타임
            Long runtime = firstMovie.optLong("runtime", 0);

            // 줄거리
            JSONObject plotsObject = firstMovie.optJSONObject("plots");
            JSONArray plotArray = plotsObject != null ? plotsObject.optJSONArray("plot") : null;
            String firstPlotText = "";
            if (plotArray != null && plotArray.length() > 0) {
                JSONObject firstPlot = plotArray.optJSONObject(0);
                firstPlotText = firstPlot != null ? firstPlot.optString("plotText", "") : "";
            }

            // 관람 등급
            String rating = firstMovie.optString("rating", "정보 없음");

            return new MovieInfoDto(director, actorNames.toString(), rating, firstPosterUrl, modifiedVodUrl, runtime, firstPlotText);
        }
        return null;
    }

    public String fetchVodUrlFromYoutube(String query) {
        // YouTube 검색 API URL을 구성합니다.
        URI youtubeSearchUrl = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("q", query)
                .queryParam("maxResults", 5)
                .queryParam("type", "video")
                .queryParam("key", youtubeApiKey)
                .build()
                .encode()
                .toUri();

        // YouTube API 호출 및 응답 수신
        String response = restTemplate.getForObject(youtubeSearchUrl, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray items = jsonResponse.getJSONArray("items");

        if (items.length() > 0) {
            JSONObject firstItem = items.getJSONObject(0);
            JSONObject id = firstItem.getJSONObject("id");
            String videoId = id.getString("videoId");
            log.debug("videoId = {}", videoId);
            return "https://www.youtube.com/embed/" + videoId;
        } else {
            return "No results found";
        }
    }

//    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
//    public void scheduleTaskUsingCronExpression() {
//        fetchAndStoreBoxOfficeData();
//    }



    public List<BoxOffice> findAll() {
        return boxOfficeRepository.findAll();
    }
}

