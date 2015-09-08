package com.example.nick.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by NICK on 9/2/2015.
 */
public class TrailerAdapter extends ArrayAdapter<Trailer> {

    private List<Trailer> mTrailers;

    public TrailerAdapter(Context context, List<Trailer> trailers) {
        super(context, 0, trailers);
        mTrailers = trailers;
    }

    @Override
    public Trailer getItem(int position) {
        return mTrailers.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View trailerView;

        Trailer t = getItem(position);

        if (convertView == null) {
            trailerView = View.inflate(getContext(), R.layout.trailer, null);

        } else {
            trailerView = convertView;
        }

        TextView trailerTitle = (TextView) trailerView.findViewById(R.id.trailer_title);
        ImageView trailerImage = (ImageView) trailerView.findViewById(R.id.trailer_image);

        String trailerThumbUrl = UrlHelper.getTrailerThumbUrl(t);

        Picasso.with(getContext())
                .load(trailerThumbUrl)
                .placeholder(R.drawable.default_movie_image)
                .error(R.drawable.noposter)
                .into(trailerImage);
        trailerTitle.setText(t.name);

        return trailerView;
    }

}
