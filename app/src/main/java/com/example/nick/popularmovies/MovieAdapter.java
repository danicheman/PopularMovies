package com.example.nick.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by NICK on 7/15/2015.
 * Adapt the movie data to the grid view
 */
public class MovieAdapter extends CursorAdapter {

    final String LOG_TAG = MoviesFragment.class.getSimpleName();

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
        this.movies = movies;
    }

    @Override
    public Movie getItem(int position) {
        return movies.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView moviePosterView;

        if (convertView == null) {
            moviePosterView = new ImageView(getContext());
            moviePosterView.setAdjustViewBounds(true);
            moviePosterView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            moviePosterView = (ImageView) convertView;
        }

        Movie m = movies.get(position);
        String movieImageLink = UrlHelper.getMovieImageLink(m);

        if(movieImageLink != null ) {
            Picasso.with(getContext())
                    .load(UrlHelper.getMovieImageLink(m))
                    .placeholder(R.drawable.default_movie_image)
                    .error(R.drawable.noposter)
                    .into(moviePosterView);
        } else {
            moviePosterView.setImageResource(R.drawable.noposter);
        }
        return moviePosterView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }


}
