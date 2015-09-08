package com.example.nick.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by NICK on 8/12/2015.
 */
public class Review implements Parcelable{

    public static final Parcelable.Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[0];
        }
    };
    private static final String KEY_MOVIE_ID = "movie_id";
    private static final String KEY_REVIEW_ID = "review_id";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_LINK = "link";
    public String review_id;
    public int movie_id;
    public String author;
    public String content;
    public String link;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
