package com.example.nick.popularmovies;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.nick.popularmovies.data.MovieContract;
import com.example.nick.popularmovies.data.MovieContract.MovieEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieReviewsEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieTrailersEntry;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetailFragment extends Fragment {
    static final String DETAIL_URI = "URI";
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";


    private static final int LOADER_MOVIE_DETAIL    = 0;
    private static final int LOADER_MOVIE_REVIEW    = 1;
    private static final int LOADER_MOVIE_TRAILER   = 2;
    private static final String MOVIE_ID_ARG = "movie_id";
    //projection
    private static final String[] MOVIE_DETAIL_COLUMNS = {
            //MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_SYNOPSIS,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_IMAGE_LINK,
    };

    private static final String[] MOVIE_REVIEWS_COLUMNS = {
            MovieReviewsEntry.COLUMN_REVIEW,
            MovieReviewsEntry.COLUMN_REVIEW_LINK,
            MovieReviewsEntry.COLUMN_AUTHOR,
    };

    private static final String[] MOVIE_TRAILERS_COLUMNS = {
            MovieTrailersEntry.COLUMN_NAME,
            MovieTrailersEntry.COLUMN_KEY,
    };


    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;
    private LoaderManager.LoaderCallbacks<Cursor> MovieLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            if (args != null) {

                long movieId = args.getLong(MOVIE_ID_ARG, 0);
                switch (id) {
                    case LOADER_MOVIE_DETAIL:
                        return new CursorLoader(getActivity(),
                                MovieContract.MovieEntry.buildMovieUri(movieId),
                                MOVIE_DETAIL_COLUMNS,
                                null,
                                null,
                                null);

                    case LOADER_MOVIE_REVIEW:
                        return new CursorLoader(getActivity(),
                                MovieContract.MovieReviewsEntry.buildReviewUri(movieId),
                                MOVIE_REVIEWS_COLUMNS,
                                null,
                                null,
                                null);

                    case LOADER_MOVIE_TRAILER:
                        return new CursorLoader(getActivity(),
                                MovieContract.MovieTrailersEntry.buildTrailerUri(movieId),
                                MOVIE_TRAILERS_COLUMNS,
                                null,
                                null,
                                null);
                }
            }

            Log.e(LOG_TAG, "Unexpected switch case outcome due to unknown loader id");
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            /**
             * Pull out the data, pop it into the views
             */
            if (data != null && data.moveToFirst()) {
                switch (loader.getId()) {
                    case LOADER_MOVIE_DETAIL:

                        break;
                    case LOADER_MOVIE_REVIEW:
                        break;
                    case LOADER_MOVIE_TRAILER:
                        break;

                }

            }
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for movie data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(MoviesFragment.MOVIE_BUNDLE)) {
            Movie m = (Movie) intent.getParcelableExtra(MoviesFragment.MOVIE_BUNDLE);

            Resources res = getResources();
            DecimalFormat df = new DecimalFormat("##.#");



            SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputDate = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

            try {
                Date date = inputDate.parse(m.releaseDate);
                String formattedReleaseDate = outputDate.format(date);

                ((TextView) rootView.findViewById(R.id.movie_title)).setText(m.title);
                ((TextView) rootView.findViewById(R.id.synopsis)).setText(Html.fromHtml(res.getText(R.string.synopsis) + " " + m.synopsis));
                ((TextView) rootView.findViewById(R.id.release_date)).setText(Html.fromHtml(res.getText(R.string.release_date) + " " + formattedReleaseDate));
                ((TextView) rootView.findViewById(R.id.rating_data)).setText(Html.fromHtml(res.getText(R.string.vote_average) + " " + df.format(m.userRating)));
                ((RatingBar) rootView.findViewById(R.id.rating_bar)).setRating(m.userRating.floatValue());
                ImageView backgroundMovieImage = (ImageView) rootView.findViewById(R.id.movie_image);
                Picasso.with(getActivity())
                        .load(UrlHelper.getMoviePosterLink(m))
                        .into(backgroundMovieImage);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return rootView;
    }
}