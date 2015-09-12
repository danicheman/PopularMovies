package com.example.nick.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nick.popularmovies.data.MovieContract;
import com.example.nick.popularmovies.data.MovieContract.MovieEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieReviewsEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieTrailersEntry;
import com.example.nick.popularmovies.views.ExpandableHeightGridView;
import com.example.nick.popularmovies.views.ExpandableHeightListView;
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

public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //referencing and getting data from MainActivity with these constants
    static final String DETAIL_URI = "URI";
    static final String MOVIE_REFERENCE = "MOVIE";

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final String MOVIE_SHARE_HASHTAG = " #PopularMoviesApp";
    private static final String KEY_TRAILERS_LIST = "mTrailersList";
    private static final String KEY_REVIEWS_LIST = "mReviewsList";
    private static final String KEY_MOVIE = "movie";
    private static final String MOVIE_ID_ARG = "movie_id";

    private static final int LOADER_MOVIE_DETAIL    = 0;
    private static final int LOADER_MOVIE_REVIEW    = 1;
    private static final int LOADER_MOVIE_TRAILER   = 2;

    //projection
    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_SYNOPSIS,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_IMAGE_LINK,
    };


    private static final String[] MOVIE_REVIEWS_COLUMNS = {
            MovieReviewsEntry.COLUMN_MOVIE_ID,
            MovieReviewsEntry.COLUMN_REVIEW_ID,
            MovieReviewsEntry.COLUMN_AUTHOR,
            MovieReviewsEntry.COLUMN_REVIEW,
            MovieReviewsEntry.COLUMN_REVIEW_LINK

    };

    private static final String[] MOVIE_TRAILERS_COLUMNS = {
            MovieTrailersEntry.COLUMN_MOVIE_ID,
            MovieTrailersEntry.COLUMN_KEY,
            MovieTrailersEntry.COLUMN_NAME

    };


    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private Movie movie;
    private ExpandableHeightGridView trailerGrid;
    private ExpandableHeightListView reviewList;

    //todo: create review adapter class
    private ArrayList<Review> mReviews;
    private ReviewAdapter mReviewAdapter;

    private ArrayList<Trailer> mTrailers;
    private TrailerAdapter mTrailerAdapter;

    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_detail, menu);

        //Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        //If we have the trailers, set them.
        if (mTrailers != null && mTrailers.size() > 0) {
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

        //if the movie isn't set try to set it from the intent
        //if(movie == null) {
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(MoviesFragment.MOVIE_BUNDLE)) {
            movie = intent.getParcelableExtra(MoviesFragment.MOVIE_BUNDLE);
            Log.d(LOG_TAG,
                    "Got movie through bundle");
            if (movie.isFavorite) {
                Log.d(LOG_TAG, "Seems movie was created from Cursor");
            } else {
                Log.d(LOG_TAG, "Seems movie was  NOOOOT created from Cursor");
            }
            }
        //}


        if (movie != null && movie.id != 0) {
            fetchDetails();
        }
    }

    private void fetchDetails() {

        if (movie.isFavorite) {
            Log.d(LOG_TAG, "USING Loader to fetch movie details for " + movie.title);
            getLoaderManager().initLoader(LOADER_MOVIE_REVIEW, null, this);
            getLoaderManager().initLoader(LOADER_MOVIE_TRAILER, null, this);
        } else if (UrlHelper.API_KEY == null) {
            Toast toast = Toast.makeText(getActivity(), "Please set the API KEY in the URL Helper", Toast.LENGTH_LONG);
            toast.show();
            return;
        } else {
            Log.d(LOG_TAG, "USING API TO fetch movie details for " + movie.title);

            FetchMovieDetailsTask detailsTask = new FetchMovieDetailsTask();
            detailsTask.execute(movie.id);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }


        //get sort order or default_movie_image value
        //String sortOrder = prefs.getString("sort_order", getResources().getStringArray(R.array.sort_order_option_values)[0]);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //getLoaderManager().initLoader(LOADER_MOVIE_DETAIL, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mTrailers = new ArrayList<Trailer>();
        mReviews = new ArrayList<Review>();

        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "!!Loading from savedInstanceState!!");
            if (savedInstanceState.containsKey(KEY_MOVIE))
                movie = savedInstanceState.getParcelable(KEY_MOVIE);
            if (savedInstanceState.containsKey(KEY_TRAILERS_LIST)) {
                mTrailers = savedInstanceState.getParcelableArrayList(KEY_TRAILERS_LIST);
            }
            if (savedInstanceState.containsKey(KEY_REVIEWS_LIST)) {
                mReviews = savedInstanceState.getParcelableArrayList(KEY_REVIEWS_LIST);
            }
        }

        mReviewAdapter = new ReviewAdapter(getActivity(), R.layout.review, mReviews);

        reviewList = (ExpandableHeightListView) rootView.findViewById(R.id.review_grid);
        reviewList.setAdapter(mReviewAdapter);

        mTrailerAdapter = new TrailerAdapter(getActivity(), mTrailers);

        trailerGrid = (ExpandableHeightGridView) rootView.findViewById(R.id.trailer_grid);
        trailerGrid.setAdapter(mTrailerAdapter);
        trailerGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.getParcelable(MOVIE_REFERENCE) != null) {
                movie = arguments.getParcelable(MOVIE_REFERENCE);
                Log.d(LOG_TAG, "got movie!");
            } else {
                Log.d(LOG_TAG, "didn't get movie :(");
            }

        }


        // The detail Activity called via intent.  Inspect the intent for movie data.
        if (movie == null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(MoviesFragment.MOVIE_BUNDLE)) {
                movie = intent.getParcelableExtra(MoviesFragment.MOVIE_BUNDLE);
            }
        }

        //return a blank view if we've initialized with no movie.
        if (movie == null) {
            rootView.setVisibility(View.GONE);
            return rootView;
        }
        Resources res = getResources();
        DecimalFormat df = new DecimalFormat("##.#");


        SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputDate = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

        try {
            Date date = inputDate.parse(movie.releaseDate);
            String formattedReleaseDate = outputDate.format(date);

            if (movie.isFavorite) {
                int[] favoriteButtonState = new int[]{android.R.attr.state_checked};
                ((ImageButton) rootView.findViewById(R.id.favorite)).setImageState(favoriteButtonState, false);
                ((TextView) rootView.findViewById(R.id.label_favorite)).setText(getResources().getText(R.string.remove_favorite));

            } else {
                //let it be in it's default button state
                ((TextView) rootView.findViewById(R.id.label_favorite)).setText(getResources().getText(R.string.add_favorite));
            }

            ((TextView) rootView.findViewById(R.id.movie_title)).setText(movie.title);
            ((TextView) rootView.findViewById(R.id.synopsis)).setText(Html.fromHtml(res.getText(R.string.synopsis) + " " + movie.synopsis));
            ((TextView) rootView.findViewById(R.id.release_date)).setText(Html.fromHtml(res.getText(R.string.release_date) + " " + formattedReleaseDate));
            ((TextView) rootView.findViewById(R.id.rating_data)).setText(Html.fromHtml(res.getText(R.string.vote_average) + " " + df.format(movie.userRating)));
            ((RatingBar) rootView.findViewById(R.id.rating_bar)).setRating(movie.userRating.floatValue());
            rootView.findViewById(R.id.favorite).setOnClickListener(new AdapterView.OnClickListener() {

                @Override
                public void onClick(View button) {

                    button.setSelected(!button.isSelected());
                    CharSequence toastMsg;

                    if (!movie.isFavorite) {
                        movie.isFavorite = true;

                        //add favorite
                        SaveMovieDetailsTask saveMovieDetailsTask = new SaveMovieDetailsTask();
                        saveMovieDetailsTask.execute();

                        toastMsg = " will be added to your favorites";
                    } else {
                        movie.isFavorite = false;

                        //remove favorite
                        DeleteMovieDetailsTask deleteMovieDetailsTask = new DeleteMovieDetailsTask();
                        deleteMovieDetailsTask.execute();

                        int[] favoriteButtonState = new int[]{};
                        ((ImageButton) getView().findViewById(R.id.favorite)).setImageState(favoriteButtonState, false);

                        toastMsg = " will be removed from your favorites";

                        //trigger finish to go back ?

                        //do toast?

                    }
                    Toast.makeText(getActivity(), movie.title + toastMsg, Toast.LENGTH_SHORT).show();

                }
            });
            ImageView backgroundMovieImage = (ImageView) rootView.findViewById(R.id.movie_image);
            Picasso.with(getActivity())
                    .load(UrlHelper.getMoviePosterLink(movie))
                    .into(backgroundMovieImage);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_MOVIE_DETAIL:
                return new CursorLoader(getActivity(),
                        MovieContract.MovieEntry.buildMovieUri(movie.id),
                        MOVIE_DETAIL_COLUMNS,
                        null,
                        null,
                        null);

            case LOADER_MOVIE_REVIEW:
                return new CursorLoader(getActivity(),
                        MovieContract.MovieReviewsEntry.buildReviewUri(movie.id),
                        MOVIE_REVIEWS_COLUMNS,
                        null,
                        null,
                        null);

            case LOADER_MOVIE_TRAILER:
                return new CursorLoader(getActivity(),
                        MovieContract.MovieTrailersEntry.buildTrailerUri(movie.id),
                        MOVIE_TRAILERS_COLUMNS,
                        null,
                        null,
                        null);
        }
        Log.e(LOG_TAG, "Unexpected switch case outcome due to unknown loader id");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //could swap a cursor in a CursorAdapter, but we'll populate the data into our existing objects
        switch (loader.getId()) {
            //MOVIE DATA SHOULD ALWAYS BE PASSED BY THE PARENT VIEW
            /*case LOADER_MOVIE_DETAIL:
                break;*/
            case LOADER_MOVIE_REVIEW:
                Log.d(LOG_TAG, "load finished, got " + data.getCount() + " Reviews.");
                mReviews = new ArrayList<Review>(data.getCount());
                if (data.moveToFirst()) {
                    //populate movie review array
                    do {

                        mReviews.add(new Review(data));
                    } while (data.moveToNext());
                    mReviewAdapter.addAll(mReviews);
                    reviewList.setExpanded(true);
                }
                break;
            case LOADER_MOVIE_TRAILER:
                Log.d(LOG_TAG, "load finished, got " + data.getCount() + " Trailers.");
                mTrailers = new ArrayList<Trailer>(data.getCount());

                //populate movie trailer array
                while (data.moveToNext()) {
                    mTrailers.add(new Trailer(data));
                }
                mTrailerAdapter.addAll(mTrailers);
                trailerGrid.setExpanded(true);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_MOVIE, movie);
        outState.putParcelableArrayList(KEY_REVIEWS_LIST, mReviews);
        outState.putParcelableArrayList(KEY_TRAILERS_LIST, mTrailers);
    }

    public interface Callback {
        void refreshMovieGrid();
    }

    //movie is a favorite and user de-selects the star
    public class DeleteMovieDetailsTask extends AsyncTask<Void, Void, Void> {

        private boolean deleteTrailers() {
            int trailersDeleted = getActivity().getContentResolver().delete(
                    MovieTrailersEntry.CONTENT_URI,
                    MovieTrailersEntry.COLUMN_MOVIE_ID + " = " + movie.id,
                    null);
            if (trailersDeleted == -1) {
                Log.e(LOG_TAG, "error deleting trailers, got back -1");
            } else {
                return true;
            }

            return false;
        }

        private boolean deleteReviews() {
            int reviewsDeleted = getActivity().getContentResolver().delete(
                    MovieReviewsEntry.CONTENT_URI,
                    MovieReviewsEntry.COLUMN_MOVIE_ID + " = " + movie.id,
                    null);
            if (reviewsDeleted == -1) {
                Log.e(LOG_TAG, "error deleting reviews, got back -1");
            } else {
                return true;
            }

            return false;
        }

        private boolean deleteMovie() {
            //Log.d("DeleteMovieTask", "Deleting movie:" + movie.title);
            int moviesDeleted = getActivity().getContentResolver().delete(
                    MovieEntry.CONTENT_URI,
                    MovieEntry._ID + " = " + movie.id,
                    null);
            if (moviesDeleted == -1) {
                Log.e(LOG_TAG, "error deleting movie, got back -1");
            } else {
                return true;
            }

            return false;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Boolean dt = deleteTrailers();
            Boolean dr = deleteReviews();
            Boolean dm = deleteMovie();

            CharSequence text;
            //toast movie removed from favorites
            /*if (dt && dr && dm) {
                text = movie.title + " has been removed from your favorites.";
            } else {
                text = "Error removing " + movie.title + " from your favorites.";
            }*/
            //Log.d(LOG_TAG, text.toString());
            /*int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getActivity(), text, duration);
            toast.show();*/

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Log.d(LOG_TAG, "refreshing favorites");
            ((Callback) getActivity()).refreshMovieGrid();
            super.onPostExecute(aVoid);
        }
    }

    public class SaveMovieDetailsTask extends AsyncTask<String, Void, Boolean> {

        private boolean saveMovie() {
            //pull movie from outer class
            Cursor movieCursor = getActivity().getContentResolver().query(
                    MovieEntry.buildMovieUri(movie.id),
                    new String[]{MovieEntry._ID},
                    MovieEntry._ID + " = ?",
                    new String[]{Integer.toString(movie.id)},
                    null
            );
            if (movieCursor.moveToFirst()) {
                Log.d(LOG_TAG, "Cant save movie, same id already exists in db." + movie.id);
                return false;
            } else {
                ContentValues movieValues = new ContentValues();

                //move moviedata to contentvalues
                movieValues.put(MovieEntry._ID, movie.id);
                movieValues.put(MovieEntry.COLUMN_TITLE, movie.title);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.releaseDate);
                movieValues.put(MovieEntry.COLUMN_SYNOPSIS, movie.synopsis);
                movieValues.put(MovieEntry.COLUMN_IMAGE_LINK, movie.imageLink);
                movieValues.put(MovieEntry.COLUMN_RATING, movie.userRating);

                Uri insertedUri = getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);
                long insertedMovieId = ContentUris.parseId(insertedUri);
                if (insertedMovieId == movie.id) {
                    return true;
                }
            }
            movieCursor.close();
            return false;
        }

        /**
         * Bulk insert reviews
         *
         * @return
         */
        private boolean saveReviews() {

            if (mReviews == null || mReviews.size() == 0) {
                return true;
            }
            //pull movie id from outer class
            Cursor reviewCursor = getActivity().getContentResolver().query(
                    MovieReviewsEntry.buildReviewUri(movie.id),
                    new String[]{MovieReviewsEntry.COLUMN_MOVIE_ID},
                    MovieReviewsEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{Integer.toString(movie.id)},
                    null
            );

            if (reviewCursor.moveToFirst()) {
                //Log.d(LOG_TAG, "Cant save reviews, " + reviewCursor.getCount() + "reviews already exist for this movie in the db." + movie.id);
                reviewCursor.close();
                return true;
            }
            reviewCursor.close();
            ContentValues[] reviewCVArray = new ContentValues[mReviews.size()];

            for (int i = 0; i < mReviews.size(); i++) {

                reviewCVArray[i] = new ContentValues();
                Review review = mReviews.get(i);
                //move review data to contentvalues
                reviewCVArray[i].put(MovieReviewsEntry.COLUMN_MOVIE_ID, review.movieId);
                reviewCVArray[i].put(MovieReviewsEntry.COLUMN_REVIEW_ID, review.reviewId);
                reviewCVArray[i].put(MovieReviewsEntry.COLUMN_AUTHOR, review.author);
                reviewCVArray[i].put(MovieReviewsEntry.COLUMN_REVIEW_LINK, review.link);
                reviewCVArray[i].put(MovieReviewsEntry.COLUMN_REVIEW, review.content);
            }

            int rowsInserted = getActivity().getContentResolver().bulkInsert(MovieReviewsEntry.CONTENT_URI, reviewCVArray);

            return rowsInserted == mReviews.size();

        }

        //TODO: bulk insert trailers
        private boolean saveTrailers() {

            if (mTrailers == null || mTrailers.size() == 0) {
                //Log.d(LOG_TAG, "No trailers for movie " + movie.title);
                return true;
            }

            //pull movie from outer class
            Cursor trailerCursor = getActivity().getContentResolver().query(
                    MovieTrailersEntry.buildTrailerUri(movie.id),
                    new String[]{MovieTrailersEntry.COLUMN_MOVIE_ID},
                    MovieTrailersEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{Integer.toString(movie.id)},
                    null
            );

            if (trailerCursor.moveToFirst()) {
                //Log.d(LOG_TAG, "Cant save trailers, " + trailerCursor.getCount() + " trailers already exist for this movie in the db.");
                trailerCursor.close();
                return true;
            }
            trailerCursor.close();
            ContentValues[] trailerCVArray = new ContentValues[mTrailers.size()];

            for (int i = 0; i < mTrailers.size(); i++) {

                trailerCVArray[i] = new ContentValues();
                Trailer trailer = mTrailers.get(i);
                //move review data to contentvalues
                //TODO: Change trailer.id to movie_id to trailer object?

                trailerCVArray[i].put(MovieTrailersEntry.COLUMN_MOVIE_ID, movie.id);
                trailerCVArray[i].put(MovieTrailersEntry.COLUMN_KEY, trailer.key);
                trailerCVArray[i].put(MovieTrailersEntry.COLUMN_NAME, trailer.name);
            }

            int rowsInserted = getActivity().getContentResolver().bulkInsert(MovieTrailersEntry.CONTENT_URI, trailerCVArray);

            if (rowsInserted == mTrailers.size()) {
                //Log.d(LOG_TAG, "Successfully saved " + rowsInserted + " trailers!!");
                return true;
            } else if (rowsInserted != mTrailers.size()) {
                //Log.e(LOG_TAG, "Rows inserted " + rowsInserted + " did not match the trailer size: " + mTrailers.size() + "trailers!!");
            }

            return false;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean sm = saveMovie();
            boolean st = saveTrailers();
            boolean sr = saveReviews();


            return (sm && st && sr);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //update da' star!
            if (result) {

                ((Callback) getActivity()).refreshMovieGrid();

                int[] favoriteButtonState = new int[]{android.R.attr.state_checked};
                ((ImageButton) getView().findViewById(R.id.favorite)).setImageState(favoriteButtonState, false);
            }
        }
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
                    mTrailerAdapter.add(t);
                }
                trailerGrid.setExpanded(true);


                //todo: fix share intent
                if (mShareActionProvider != null && mTrailers != null && mTrailers.size() > 0) {
                    //Log.d(LOG_TAG, "setting share action provider from Fetch Movie Details Task");
                    //got one or more trailers, make the first one shareable.
                    mShareActionProvider.setShareIntent(createShareFirstTrailerIntent());
                } else {
                    //Log.d(LOG_TAG, "share action provider (view) was not initialized when trailers were loaded");
                }

            }


            if (reviews != null) {
                mReviews = new ArrayList<>(Arrays.asList(reviews));
                //Log.v(LOG_TAG, "Clearing reviews");
                mReviewAdapter.clear();
                for (Review r : reviews) {
                    //Log.v(LOG_TAG, "got review from: " + r.author);
                    mReviewAdapter.add(r);
                }
                reviewList.setExpanded(true);
            }

        }

        /**
         * Get trailer details from the api and save them to a local private variable
         * @param jsonTrailerArray
         * @param movieId
         * @throws JSONException
         */
        private void getMovieTrailersFromJson(JSONArray jsonTrailerArray, int movieId) throws JSONException {

            trailers = new Trailer[jsonTrailerArray.length()];
            for (int i = 0; i < jsonTrailerArray.length(); i++) {

                JSONObject trailerData = jsonTrailerArray.getJSONObject(i);
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

                reviews[i].movieId = movieId;
                reviews[i].reviewId = reviewData.getString("id");
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
                //Log.d(LOG_TAG, "completed loading details data, parsing");

                //get trailer results
                JSONObject movieDetailJson = new JSONObject(moviesJsonStr);
                JSONArray trailerArray = movieDetailJson.getJSONObject(TMDB_TRAILERS).getJSONArray(TMDB_TRAILERS_YT);
                getMovieTrailersFromJson(trailerArray, movieId);

                //get review results
                JSONArray reviewArray = movieDetailJson.getJSONObject(TMDB_REVIEWS).getJSONArray(TMDB_RESULTS);
                getMovieReviewsFromJson(reviewArray, movieId);
                //Log.d(LOG_TAG, "finished loading details data, adding to adapters in Post Execute method");
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }
    }

}