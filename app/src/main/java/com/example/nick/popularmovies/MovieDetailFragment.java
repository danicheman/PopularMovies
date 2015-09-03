package com.example.nick.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nick.popularmovies.data.MovieContract;
import com.example.nick.popularmovies.data.MovieContract.MovieEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieReviewsEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieTrailersEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//todo: separate movie detail fragment from favorite movie detail fragment?
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
    private Uri mUri;

    private List<Review> mReviews;
    private List<Trailer> mTrailers;
    private LoaderManager.LoaderCallbacks<Cursor> FavoriteLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
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
    public void onStart() {
        super.onStart();
        fetchDetails();

    }

    private void fetchDetails() {
        if (UrlHelper.API_KEY == null) {
            Toast toast = Toast.makeText(getActivity(), "Please set the API KEY in the URL Helper", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        FetchMovieDetailsTask detailsTask = new FetchMovieDetailsTask(getActivity());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //get sort order or default_movie_image value
        String sortOrder = prefs.getString("sort_order", getResources().getStringArray(R.array.sort_order_option_values)[0]);
        detailsTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);


        TrailerAdapter mTrailerAdapter = new TrailerAdapter(getActivity(), mTrailers);

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.trailer_grid);
        gridView.setAdapter(mTrailerAdapter);

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

    /**
     * todo: fetch genres, trailers and reviews
     * todo: save data to the database
     */
    public class FetchMovieDetailsTask extends AsyncTask<Integer, Void, Void> {

        final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();

        final String TMDB_TRAILERS = "trailers";
        final String TMDB_TRAILERS_YT = "youtube";
        final String TMDB_REVIEWS = "reviews";
        final String TMDB_RESULTS = "results";

        private final Context mContext;

        public FetchMovieDetailsTask(Context c) {
            mContext = c;
        }

        //todo:create constants for the two details queries.
    /*final String TMDB_TITLE = "title";
    final String TMDB_ORIGINAL_TITLE = "original_title";
    final String TMDB_VOTE_AVERAGE = "vote_average";
    final String TMDB_IMAGE_LINK = "poster_path";
    final String TMDB_OVERVIEW = "overview";
    final String TMDB_RELEASE_DATE = "release_date";*/

        /**
         * Get details from the api and save them to the database
         */
        private void getAndSaveMovieTrailersFromJson(JSONArray jsonTrailerArray, int movieId) throws JSONException {

            //todo: fix dis shit
            mTrailers = new List<Trailer>();

            for (int i = 0; i < jsonTrailerArray.length(); i++) {

                JSONObject trailerData = jsonTrailerArray.getJSONObject(i);

                mTrailers[i] = new Trailer();
                mTrailers[i].key = trailerData.getString("key");
                mTrailers[i].name = trailerData.getString("name");

            }
        }

        /**
         * Get details from the api and save them to the database
         */
        private void getAndSaveMovieReviewsFromJson(JSONArray reviewArray, int movieId) throws JSONException {

            ArrayList<ContentValues> contentValuesArrayList = new ArrayList<>(reviewArray.length());

            for (int i = 0; i < reviewArray.length(); i++) {

                ContentValues reviewValues = new ContentValues();

                JSONObject reviewData = reviewArray.getJSONObject(i);

                reviewValues.put(MovieReviewsEntry.COLUMN_REVIEW_ID, reviewData.getString("id"));
                reviewValues.put(MovieReviewsEntry.COLUMN_REVIEW, reviewData.getString("content"));
                reviewValues.put(MovieReviewsEntry.COLUMN_REVIEW_LINK, reviewData.getString("url"));
                reviewValues.put(MovieReviewsEntry.COLUMN_AUTHOR, reviewData.getString("author"));
                reviewValues.put(MovieReviewsEntry.COLUMN_MOVIE_ID, movieId);

                contentValuesArrayList.add(reviewValues);
            }
            if (contentValuesArrayList.size() > 0) {
                ContentValues[] contentValuesArray = new ContentValues[contentValuesArrayList.size()];
                contentValuesArrayList.toArray(contentValuesArray);
                mContext.getContentResolver().bulkInsert(MovieContract.MovieReviewsEntry.CONTENT_URI, contentValuesArray);
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length == 0) {
                return null;
            }

            int movieId = params[0];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                // Construct the URL for the themoviedb.org query
                // Possible parameters are avaiable at TMDB's API page, at
                // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie
                URL url = UrlHelper.getMovieDetailUrl(movieId);

                // Create the request to themoviedb.org, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {

                //get trailer results
                JSONObject movieDetailJson = new JSONObject(moviesJsonStr);
                JSONArray trailerArray = movieDetailJson.getJSONObject(TMDB_TRAILERS).getJSONArray(TMDB_TRAILERS_YT);
                getAndSaveMovieTrailersFromJson(trailerArray, movieId);

                //get review results
                JSONArray reviewArray = movieDetailJson.getJSONObject(TMDB_REVIEWS).getJSONArray(TMDB_RESULTS);
                getAndSaveMovieReviewsFromJson(reviewArray, movieId);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }
    }
}