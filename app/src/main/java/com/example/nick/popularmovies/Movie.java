package com.example.nick.popularmovies;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by NICK on 7/15/2015.
 *
 * Will need to use a movie object because we cannot simply return a database result.
 * Details page needs to load two types of data, trailers and reviews.
 */
public class Movie implements Parcelable{

    //column ids
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_TITLE = 1;
    public static final int COLUMN_RELEASE_DATE = 2;
    public static final int COLUMN_SYNOPSIS = 3;
    public static final int COLUMN_IMAGE_LINK = 4;
    public static final int COLUMN_RATING = 5;
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_RELEASE_DATE = "releaseDate";
    private static final String KEY_IMAGE_LINK = "imageLink";
    private static final String KEY_SYNOPSIS = "synopsis";
    private static final String KEY_USER_RATING = "userRating";
    private static final String KEY_ORIGINAL_TITLE = "originalTitle";
    private static final String KEY_IS_FAVORITE = "isFavorite";
    public int    id;
    public String title;
    public String originalTitle;
    public String releaseDate;
    public String imageLink;
    public String synopsis; //overview
    public Double userRating; //vote_average
    public boolean isFavorite;
    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();

            Movie movie = new Movie();

            movie.id            = bundle.getInt(KEY_ID, 0);
            movie.title         = bundle.getString(KEY_TITLE, null);
            movie.releaseDate   = bundle.getString(KEY_RELEASE_DATE, null);
            movie.synopsis      = bundle.getString(KEY_SYNOPSIS, null);
            movie.userRating    = bundle.getDouble(KEY_USER_RATING, 0);
            movie.imageLink = bundle.getString(KEY_IMAGE_LINK, null);
            movie.isFavorite = bundle.getBoolean(KEY_IS_FAVORITE, false);
            return movie;
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[0];
        }
    };

    public Movie() {
        isFavorite = false;
    }

    public Movie(Cursor cursor) {

        id = cursor.getInt(COLUMN_ID);
        title = cursor.getString(COLUMN_TITLE);
        releaseDate = cursor.getString(COLUMN_RELEASE_DATE);
        imageLink = cursor.getString(COLUMN_IMAGE_LINK);
        synopsis = cursor.getString(COLUMN_SYNOPSIS);
        userRating = cursor.getDouble(COLUMN_RATING);

        //created from database so it's a favorite!
        isFavorite = true;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        //create the bundle and insert the key/value pairs into it
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ID, id);
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_RELEASE_DATE, releaseDate);
        bundle.putString(KEY_IMAGE_LINK, imageLink);
        bundle.putString(KEY_SYNOPSIS, synopsis);
        bundle.putString(KEY_ORIGINAL_TITLE, originalTitle);
        bundle.putDouble(KEY_USER_RATING, userRating);
        bundle.putBoolean(KEY_IS_FAVORITE, isFavorite);

        //write the key value pairs to the parcel
        dest.writeBundle(bundle);
    }
}
