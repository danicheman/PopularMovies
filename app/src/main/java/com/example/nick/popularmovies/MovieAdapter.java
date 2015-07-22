package com.example.nick.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by NICK on 7/15/2015.
 * Adapt the movie data to the grid view
 */
public class MovieAdapter extends ArrayAdapter<Movie>{
    private List<Movie> movies;
    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";
    final String LOG_TAG = MoviesFragment.class.getSimpleName();

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
        this.movies = movies;
        Log.v(LOG_TAG, "constructing MovieAdapter");
    }

    @Override
    public Movie getItem(int position) {
        return movies.get(position);
    }

    //185x278
    //
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.v(LOG_TAG, "movie adapter getting view at" + position);
        ImageView moviePosterView;

        if (convertView == null) {
            moviePosterView = new ImageView(getContext());
            //moviePosterView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //moviePosterView.setLayoutParams(new GridView.LayoutParams(185,278));
            //moviePosterView.setScaleType(ImageView.ScaleType.FIT_XY);
            moviePosterView.setAdjustViewBounds(true);
            moviePosterView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            moviePosterView = (ImageView) convertView;
        }

        //.placeholder(R.raw.place_holder)
        //.error(R.raw.big_problem)
        Picasso.with(getContext())
                .load(getMovieImageLinkAt(position))
                .into(moviePosterView);

        return moviePosterView;
    }

    private String getMovieImageLinkAt(int index) {
        Movie m = movies.get(index);
        return BASE_IMAGE_URL + m.imageLink;
    }
}
