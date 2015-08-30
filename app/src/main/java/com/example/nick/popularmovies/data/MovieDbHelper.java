package com.example.nick.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nick.popularmovies.data.MovieContract.GenreEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieGenresEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieReviewsEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieTrailersEntry;

/**
 * Created by NICK on 8/10/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 2;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        //create the movies table, remember if the movie is a favorite and if we've loaded both reviews and trailers (details)
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieEntry.COLUMN_TITLE + " TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_IMAGE_LINK + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0, " +
                MovieEntry.COLUMN_GOT_DETAILS + " INTEGER DEFAULT 0 " +
                " )";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);

        //trailer table many to one with movies
        final String SQL_CREATE_MOVIE_TRAILERS_TABLE = "CREATE TABLE " +
                MovieTrailersEntry.TABLE_NAME + " (" +
                MovieTrailersEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieTrailersEntry.COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
                MovieTrailersEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MovieTrailersEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +
                " UNIQUE (" + MovieTrailersEntry.COLUMN_MOVIE_ID + ", " +
                MovieTrailersEntry.COLUMN_KEY + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_TRAILERS_TABLE);

        //review table, many to one with movies
        final String SQL_CREATE_MOVIE_REVIEWS_TABLE = "CREATE TABLE " +
                MovieReviewsEntry.TABLE_NAME + " (" +
                MovieReviewsEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieReviewsEntry.COLUMN_REVIEW_ID + " TEXT UNIQUE NOT NULL, " +
                MovieReviewsEntry.COLUMN_REVIEW + " TEXT NOT NULL, " +
                MovieReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MovieReviewsEntry.COLUMN_REVIEW_LINK + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MovieReviewsEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +
                " UNIQUE (" + MovieReviewsEntry.COLUMN_MOVIE_ID + ", " +
                MovieReviewsEntry.COLUMN_REVIEW_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_REVIEWS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GenreEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieGenresEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieReviewsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieTrailersEntry.TABLE_NAME);
        onCreate(db);
    }
}
