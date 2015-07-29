package com.example.nick.popularmovies;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
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
public class MovieAdapter extends ArrayAdapter<Movie>{
    private List<Movie> movies;

    final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private Drawable mErrorImage;

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
        this.movies = movies;
        mErrorImage = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.default_movie_image, null);
        Log.v(LOG_TAG, "constructing MovieAdapter");
    }

    @Override
    public Movie getItem(int position) {
        return movies.get(position);
    }
    //R.drawable.movies
    //185x278
    //
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
        //.placeholder(R.raw.place_holder)
        //.error(R.raw.big_problem)
        String movieImageLink = Constants.getMovieImageLink(m);

        if(movieImageLink != null ) {
            Picasso.with(getContext())
                    .load(Constants.getMovieImageLink(m))
                    .placeholder(R.drawable.default_movie_image)
                    .error(R.drawable.noposter)
                    .into(moviePosterView);
        } else {
            moviePosterView.setImageResource(R.drawable.noposter);
        }
        return moviePosterView;
    }


}
