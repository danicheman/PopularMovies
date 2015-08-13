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


    public static String getMovieImageLink(Movie m) {
        if (m.imageLink == null) return null;
        return BASE_IMAGE_URL + m.imageLink;
    }

    public static String getMoviePosterLink(Movie m) {
        if (m.imageLink == null) return null;
        return BASE_POSTER_URL + m.imageLink;
    }

    public static URL getTrailerUrl(Trailer t) throws MalformedURLException {
        if (t.key == null) return null;
        return new URL(BASE_YOUTUBE_VIDEO_URL + t.key);
    }

    public static URL getTrailerThumbUrl(Trailer t) throws MalformedURLException {
        if (t.key == null) return null;
        return new URL(YOUTUBE_IMAGE_URL_PREFIX + t.key + YOUTUBE_IMAGE_URL_SUFFIX);
    }
}