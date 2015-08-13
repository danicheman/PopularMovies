package com.example.nick.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by NICK on 8/10/2015.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.nick.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.nick.popularmovies/movie/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_FAVORITES = "favorites";

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME           = "movie";
        public static final String COLUMN_TITLE         = "title";
        public static final String COLUMN_SYNOPSIS      = "synopsis";
        public static final String COLUMN_RATING        = "rating";
        public static final String COLUMN_IMAGE_LINK    = "image_link";
        public static final String COLUMN_IS_FAVORITE   = "is_favorite";

    }

    public static final class MovieGenresEntry {
        public static final String TABLE_NAME = "movies_genres";
        public static final String COLUMN_GENRE_ID = "genre_id";
        public static final String COLUMN_MOVIE_ID = "movie_id";
    }

    //http://api.themoviedb.org/3/genre/movie/list?api_key=b8a4068d1466dca29becff1029e0e0e1
    public static final class GenreEntry implements BaseColumns {
        public static final String TABLE_NAME = "genre";
        public static final String COLUMN_TITLE = "genre";
    }


}
