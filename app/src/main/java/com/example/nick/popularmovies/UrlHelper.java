package com.example.nick.popularmovies;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by NICK on 7/20/2015.
 *
 */
public class UrlHelper {
    public static final String API_KEY = "b8a4068d1466dca29becff1029e0e0e1";
    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";
    private static final String BASE_POSTER_URL = "http://image.tmdb.org/t/p/w500";
    private static final String BASE_YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v=";
    private static final String YOUTUBE_IMAGE_URL_PREFIX = "http://img.youtube.com/vi/";
    private static final String YOUTUBE_IMAGE_URL_SUFFIX = "/0.jpg";
    private static final String TMDB_ALL_DATA_PREFIX = "http://api.themoviedb.org/3/movie/";
    private static final String TMDB_ALL_DATA_SUFFIX = "api_key=" + API_KEY + "&append_to_response=trailers,reviews";

    public static String getMovieImageLink(String imageIdentifier) {
        if (imageIdentifier == null) return null;
        return BASE_IMAGE_URL + imageIdentifier;
    }


    public static String getMoviePosterLink(Movie m) {
        if (m.imageLink == null) return null;
        return BASE_POSTER_URL + m.imageLink;
    }

    public static URL getTrailerUrl(Trailer t) throws MalformedURLException {
        if (t.key == null) return null;
        return new URL(BASE_YOUTUBE_VIDEO_URL + t.key);
    }

    public static String getTrailerThumbUrl(Trailer t) {
        if (t.key == null) return null;
        return YOUTUBE_IMAGE_URL_PREFIX + t.key + YOUTUBE_IMAGE_URL_SUFFIX;
    }

    public static URL getMovieDetailUrl(int movieId) throws MalformedURLException {
        return new URL(TMDB_ALL_DATA_PREFIX + movieId + TMDB_ALL_DATA_SUFFIX);
    }
}
