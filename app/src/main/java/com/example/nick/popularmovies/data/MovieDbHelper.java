package com.example.nick.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nick.popularmovies.data.MovieContract.*;

/**
 * Created by NICK on 8/10/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieEntry.COLUMN_TITLE + " TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_IMAGE_LINK + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0 " +
                " )";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);

        final String SQL_CREATE_GENRES_TABLE = "CREATE TABLE " +
                GenreEntry.TABLE_NAME + " (" +
                GenreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GenreEntry.COLUMN_TITLE + " TEXT UNIQUE NOT NULL);";

        db.execSQL(SQL_CREATE_GENRES_TABLE);

        //link table, references both of the previous two tables
        final String SQL_CREATE_MOVIE_GENRES_TABLE = "CREATE TABLE " +
                MovieGenresEntry.TABLE_NAME + " (" +
                MovieGenresEntry.COLUMN_GENRE_ID + " INTEGER NOT NULL, " +
                MovieGenresEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + MovieGenresEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +
                " FOREIGN KEY (" + MovieGenresEntry.COLUMN_GENRE_ID + ") REFERENCES " +
                GenreEntry.TABLE_NAME + " (" + GenreEntry._ID + "), " +
                " UNIQUE (" + MovieGenresEntry.COLUMN_MOVIE_ID + ", " +
                MovieGenresEntry.COLUMN_GENRE_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_GENRES_TABLE);

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
        onCreate(db);
    }
}
