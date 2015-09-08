package com.example.nick.popularmovies;

import android.content.ActivityNotFoundException;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

//todo: separate movie detail fragment from favorite movie detail fragment?
public class MovieDetailFragment extends Fragment {
    static final String DETAIL_URI = "URI";
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final String MOVIE_SHARE_HASHTAG = " #PopularMoviesApp";
    private static final String KEY_TRAILERS_LIST = "mTrailersList";
    private static final String KEY_REVIEWS_LIST = "mReviewsList";

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
    private Movie movie;

    //todo: create review adapter class
    private ArrayList<Review> mReviews;
    private ReviewAdapter mReviewAdapter;

    private ArrayList<Trailer> mTrailers;
    private TrailerAdapter mTrailerAdapter;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mTrailers != null) {
            mShareActionProvider.setShareIntent(createShareFirstTrailerIntent());
            Log.d(LOG_TAG, "setting share intent when creating options menu");
        } else {
            Log.d(LOG_TAG, "trailers were not available to initialize share intent");
        }
    }

    private Intent createShareFirstTrailerIntent() {
        Trailer firstTrailer = mTrailers.get(0);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        try {
            shareIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getTrailerUrl(firstTrailer).toString() + MOVIE_SHARE_HASHTAG);
        } catch (MalformedURLException mue) {
            Log.e(LOG_TAG, "Threw a malformed url exception for the movie's first trailer.");
        }
        return shareIntent;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(MoviesFragment.MOVIE_BUNDLE)) {
            movie = intent.getParcelableExtra(MoviesFragment.MOVIE_BUNDLE);
            Log.d(LOG_TAG, "clicked movie with name:" + movie.title + "with id: " + movie.id);
            if (movie.id != 0) {
                fetchDetails(movie.id);
            } else {
                Log.d(LOG_TAG, "No movie id found for " + movie.title);
            }
        }


    }

    private void fetchDetails(int movieId) {
        if (UrlHelper.API_KEY == null) {
            Toast toast = Toast.makeText(getActivity(), "Please set the API KEY in the URL Helper", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Log.d(LOG_TAG, "Fetching movie details for id" + movieId);
        FetchMovieDetailsTask detailsTask = new FetchMovieDetailsTask();
        detailsTask.execute(movieId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //get sort order or default_movie_image value
        //String sortOrder = prefs.getString("sort_order", getResources().getStringArray(R.array.sort_order_option_values)[0]);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if (savedInstanceState != null) {
            mTrailers = savedInstanceState.getParcelableArrayList(KEY_TRAILERS_LIST);
            mReviews = savedInstanceState.getParcelableArrayList(KEY_REVIEWS_LIST);
        } else {
            mTrailers = new ArrayList<Trailer>();
            mReviews = new ArrayList<Review>();
        }

        mReviewAdapter = new ReviewAdapter(getActivity(), R.layout.review, mReviews);

        ListView reviewListView = (ListView) rootView.findViewById(R.id.review_list);
        reviewListView.setAdapter(mReviewAdapter);

        mTrailerAdapter = new TrailerAdapter(getActivity(), mTrailers);

        GridView gridView = (GridView) rootView.findViewById(R.id.trailer_grid);
        gridView.setAdapter(mTrailerAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trailer clickedTrailer = mTrailerAdapter.getItem(position);
                watchYoutubeVideo(clickedTrailer.key);
            }

            //from http://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
            private void watchYoutubeVideo(String id) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + id));
                    startActivity(intent);
                }
            }
        });

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
     * todo: fetch reviews
     */
    public class FetchMovieDetailsTask extends AsyncTask<Integer, Void, Void> {

        final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();

        final String TMDB_TRAILERS = "trailers";
        final String TMDB_TRAILERS_YT = "youtube";
        final String TMDB_REVIEWS = "reviews";
        final String TMDB_RESULTS = "results";

        //private final Context mContext;
        private Review[] reviews;
        private Trailer[] trailers;

        /**
         * Populate Review and Trailer data into adapters
         *
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            if (trailers != null) {
                mTrailers = new ArrayList<>(Arrays.asList(trailers));
                mTrailerAdapter.clear();
                for (Trailer t : trailers) {
                    Log.v(LOG_TAG, "got trailer: " + t.name);
                    mTrailerAdapter.add(t);
                }

                if (mShareActionProvider != null) {
                    Log.d(LOG_TAG, "setting share action provider from Fetch Movie Details Task");
                    //got one or more trailers, make the first one shareable.
                    mShareActionProvider.setShareIntent(createShareFirstTrailerIntent());
                } else {
                    Log.d(LOG_TAG, "share action provider (view) was not initialized when trailers were loaded");
                }

            }


            if (reviews != null) {
                mReviews = new ArrayList<>(Arrays.asList(reviews));
                Log.v(LOG_TAG, "Clearing reviews");
                mReviewAdapter.clear();
                for (Review r : reviews) {
                    Log.v(LOG_TAG, "got review from: " + r.author);
                    mReviewAdapter.add(r);
                }
            }
        }

        //todo:create constants for the two details queries.
    /*final String TMDB_TITLE = "title";
    final String TMDB_ORIGINAL_TITLE = "original_title";
    final String TMDB_VOTE_AVERAGE = "vote_average";
    final String TMDB_IMAGE_LINK = "poster_path";
    final String TMDB_OVERVIEW = "overview";
    final String TMDB_RELEASE_DATE = "release_date";*/

        /**
         * Get trailer details from the api and save them to a local private variable
         * @param jsonTrailerArray
         * @param movieId
         * @throws JSONException
         */
        private void getMovieTrailersFromJson(JSONArray jsonTrailerArray, int movieId) throws JSONException {

            trailers = new Trailer[jsonTrailerArray.length()];
            Log.d(LOG_TAG, "gettig trailers from JSON");
            for (int i = 0; i < jsonTrailerArray.length(); i++) {

                JSONObject trailerData = jsonTrailerArray.getJSONObject(i);
                Log.d(LOG_TAG, "gettig trailer " + trailerData.getString("name") + " from JSON");
                trailers[i] = new Trailer();
                trailers[i].key = trailerData.getString("source");
                trailers[i].name = trailerData.getString("name");

            }
        }

        /**
         * Get review details from the api and save them to a local private variable
         *
         * @param reviewArray
         * @param movieId
         * @throws JSONException
         */
        private void getMovieReviewsFromJson(JSONArray reviewArray, int movieId) throws JSONException {

            reviews = new Review[reviewArray.length()];

            for (int i = 0; i < reviewArray.length(); i++) {

                JSONObject reviewData = reviewArray.getJSONObject(i);
                reviews[i] = new Review();

                reviews[i].movie_id = movieId;
                reviews[i].review_id = reviewData.getString("id");
                reviews[i].content = reviewData.getString("content");
                reviews[i].link = reviewData.getString("url");
                reviews[i].author = reviewData.getString("author");
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length == 0) {
                return null;
            }

            int movieId = params[0];

            if (movieId == 0) {
                Log.e(LOG_TAG, "invalid movie id!! null or zero" + movieId);
                return null;
            }
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
                Log.d(LOG_TAG, "completed loading details data, parsing");

                //get trailer results
                JSONObject movieDetailJson = new JSONObject(moviesJsonStr);
                JSONArray trailerArray = movieDetailJson.getJSONObject(TMDB_TRAILERS).getJSONArray(TMDB_TRAILERS_YT);
                getMovieTrailersFromJson(trailerArray, movieId);

                //get review results
                JSONArray reviewArray = movieDetailJson.getJSONObject(TMDB_REVIEWS).getJSONArray(TMDB_RESULTS);
                getMovieReviewsFromJson(reviewArray, movieId);
                Log.d(LOG_TAG, "finished loading details data, adding to adapters in Post Execute method");
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }
    }
}