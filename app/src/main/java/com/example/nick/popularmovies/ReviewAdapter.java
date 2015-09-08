package com.example.nick.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by NICK on 9/7/2015.
 */
public class ReviewAdapter extends ArrayAdapter {

    private List<Review> mReviews;

    public ReviewAdapter(Context context, int resource, List reviews) {
        super(context, resource, reviews);
        mReviews = reviews;
    }

    @Override
    public Review getItem(int position) {
        return mReviews.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View reviewListView;

        Review r = getItem(position);

        if (convertView == null) {
            reviewListView = View.inflate(getContext(), R.layout.review, null);

        } else {
            reviewListView = convertView;
        }

        ((TextView) reviewListView.findViewById(R.id.content)).setText(r.content);
        ((TextView) reviewListView.findViewById(R.id.author)).setText(r.author);

        return reviewListView;
    }
}
