package com.example.nick.popularmovies;

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
