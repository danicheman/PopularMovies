package com.example.nick.popularmovies;

/**
 * Created by NICK on 7/20/2015.
 * todo: change the name of this class!
 */
public class UrlHelper {
    public static final String API_KEY = null;
    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";
    private static final String BASE_POSTER_URL = "http://image.tmdb.org/t/p/w500";
    //todo: break this down

    public static String getMovieImageLink(Movie m) {
        if(m.imageLink == null) return null;
        return BASE_IMAGE_URL + m.imageLink;
    }

    public static String getMoviePosterLink(Movie m) {
        if(m.imageLink == null) return null;
        return BASE_POSTER_URL + m.imageLink;
    }
}
