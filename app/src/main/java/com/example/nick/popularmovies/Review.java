package com.example.nick.popularmovies;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by NICK on 8/12/2015.
 */
public class Review implements Parcelable{

    private static final String KEY_MOVIE_ID = "movieId";
    private static final String KEY_REVIEW_ID = "reviewId";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_LINK = "link";

    //used to contstruct from cursor
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_REVIEW_ID = 1;
    private static final int COL_AUTHOR = 2;
    private static final int COL_CONTENT = 3;
    private static final int COL_LINK = 4;


    public String reviewId;
    public int movieId;
    public String author;
    public String content;
    public String link;
    public static final Parcelable.Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();

            Review review = new Review();
            review.movieId = bundle.getInt(KEY_MOVIE_ID);
            review.reviewId = bundle.getString(KEY_REVIEW_ID);
            review.author = bundle.getString(KEY_AUTHOR);
            review.content = bundle.getString(KEY_CONTENT);
            review.link = bundle.getString(KEY_LINK);

            return review;
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[0];
        }
    };

    //main constructor with no args
    public Review() {
    }

    //constructor using cursor
    public Review(Cursor cursor) {
        movieId = cursor.getInt(COL_MOVIE_ID);
        reviewId = cursor.getString(COL_REVIEW_ID);
        author = cursor.getString(COL_AUTHOR);
        content = cursor.getString(COL_CONTENT);
        link = cursor.getString(COL_LINK);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //save the Review
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();

        bundle.putInt(KEY_MOVIE_ID, movieId);
        bundle.putString(KEY_REVIEW_ID, reviewId);
        bundle.putString(KEY_AUTHOR, author);
        bundle.putString(KEY_CONTENT, content);
        bundle.putString(KEY_LINK, link);

        dest.writeBundle(bundle);
    }
}
