package com.example.nick.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by NICK on 7/15/2015.
 * Adapt the movie data to the grid view
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private List<Movie> movies;

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
            moviePosterView = (ImageView) View.inflate(getContext(), R.layout.movie_grid_cell, null);
            moviePosterView.setAdjustViewBounds(true);
            moviePosterView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            moviePosterView = (ImageView) convertView;
        }

        Movie m = movies.get(position);
        String movieImageLink = UrlHelper.getMovieImageLink(m.imageLink);
        //Log.d("MovieAdapter", "loading image" + movieImageLink);
        if(movieImageLink != null ) {
            Picasso.with(getContext())
                    .load(movieImageLink)
                    .placeholder(R.drawable.default_movie_image)
                    .error(R.drawable.noposter)
                    .into(moviePosterView);
        } else {
            moviePosterView.setImageResource(R.drawable.noposter);
        }
        return moviePosterView;
    }


}