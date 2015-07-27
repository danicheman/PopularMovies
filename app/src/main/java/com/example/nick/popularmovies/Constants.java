package com.example.nick.popularmovies;

/**
 * Created by NICK on 7/20/2015.
 * todo: change the name of this class!
 */
public class Constants {
    public final String API_KEY = "b8a4068d1466dca29becff1029e0e0e1";
    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";
    //todo: break this down
    public final String API_URL_BASE = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + API_KEY;

    public static String getMovieImageLink(Movie m) {

        return BASE_IMAGE_URL + m.imageLink;
    }
}
