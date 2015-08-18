package com.example.nick.popularmovies;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by NICK on 7/15/2015.
 */
public class Movie implements Parcelable{

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_RELEASE_DATE = "releaseDate";
    private static final String KEY_IMAGE_LINK = "imageLink";
    private static final String KEY_SYNOPSIS = "synopsis";
    private static final String KEY_USER_RATING = "userRating";
    private static final String KEY_ORIGINAL_TITLE = "originalTitle";

    public int    id;
    public String title;
    public String originalTitle;
    public String releaseDate;
    public String imageLink;
    public String synopsis; //overview
    public Double userRating; //vote_average

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

        //write the key value pairs to the parcel
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();

            Movie movie = new Movie();

            movie.id            = bundle.getInt(KEY_ID, 0);
            movie.title         = bundle.getString(KEY_TITLE, null);
            movie.releaseDate   = bundle.getString(KEY_RELEASE_DATE, null);
            movie.imageLink     = bundle.getString(KEY_IMAGE_LINK, null);
            movie.synopsis      = bundle.getString(KEY_SYNOPSIS, null);
            movie.originalTitle = bundle.getString(KEY_ORIGINAL_TITLE, null);
            movie.userRating    = bundle.getDouble(KEY_USER_RATING, 0);

            return movie;
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[0];
        }
    };
}
