package com.example.nick.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by NICK on 7/15/2015.
 * Adapt the movie data to the grid view
 * Populates from a database cursor
 */
public class MovieDbAdapter extends CursorAdapter {

    final String LOG_TAG = MoviesFragment.class.getSimpleName();

    public MovieDbAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*@Override
    public Movie getItem(int position) {
        return movies.get(position);
    }*/

    /*@Override
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
    }*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        /*int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TABLET:
                layoutId = R.layout.list_item_highlight;
                break;
            case VIEW_TYPE_MOBILE:
                layoutId = R.layout.list_item_regular;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);*/

        return new ImageView(context);
    }

    //magic happens here now.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView moviePosterView;
        moviePosterView = (ImageView) view;

        String imageId = cursor.getString(MoviesFragment.COL_IMAGE_LINK);
        String movieImageLink = UrlHelper.getMovieImageLink(imageId);

        if (movieImageLink != null) {
            Picasso.with(context)
                    .load(movieImageLink)
                    .placeholder(R.drawable.default_movie_image)
                    .error(R.drawable.noposter)
                    .into(moviePosterView);
        } else {
            moviePosterView.setImageResource(R.drawable.noposter);
        }
    }
}
