package com.example.nick.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nick.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_MOVIES_LIST = "mMovieList";
    public static final String MOVIE_BUNDLE = "movieBundle";

    private static final String SELECTED_KEY = "selected_position";
    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_SYNOPSIS,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_IMAGE_LINK
    };
    final String LOG_TAG = MoviesFragment.class.getSimpleName();
    public String mSortOrder;
    //The adapter to prepare the data for the view
    private MovieAdapter mMovieAdapter;
    //The array of movies retrieved from the server
    private ArrayList<Movie> mMovieList;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;
    public MoviesFragment() {
        // Required empty public constructor

    }

    /*
    //bad form - no
    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_MOVIES_LIST, mMovieList);
    }


    public void updateMovies(String sortOrder) {
        if(UrlHelper.API_KEY == null) {
            Toast toast = Toast.makeText(getActivity(), "Please set the API KEY in the URL Helper", Toast.LENGTH_LONG);
            toast.show();
            return;
        }


        if (!sortOrder.equals("favorites")) {
            mMovieAdapter.clear();
            //Log.d(LOG_TAG, "some other sort order" + sortOrder);
            //get movies from website
            FetchMoviesTask moviesTask = new FetchMoviesTask();
            moviesTask.execute(sortOrder);
        } else {
            //Log.d(LOG_TAG, "favorites sort order");
            //get movies from database - if there were more than one type of data selection,
            // we might use restartLoader() here.
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSortOrder = ((Callback) getActivity()).getSortOrder();

        if (savedInstanceState == null && mSortOrder.equals("favorites")) {
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        } else if (savedInstanceState != null) {
            Log.e(LOG_TAG, "Returning from saved state?");
            if (savedInstanceState.containsKey(KEY_MOVIES_LIST)) {
                mMovieList = savedInstanceState.getParcelableArrayList(KEY_MOVIES_LIST);
            }
        } else {
            FetchMoviesTask fmt = new FetchMoviesTask();
            fmt.execute(mSortOrder);
        }
        super.onActivityCreated(savedInstanceState);
    }

    //Based on a stackoverflow snippet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mMovieList = savedInstanceState.getParcelableArrayList(KEY_MOVIES_LIST);

        } else mMovieList = new ArrayList<>();

        mMovieAdapter = new MovieAdapter(getActivity(), mMovieList);

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movieGridView);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie clickedMovie = (Movie) parent.getItemAtPosition(position);
                mPosition = position;
                ((Callback) getActivity()).onItemSelected(clickedMovie);
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //sort order doesn't matter for favorites.
        //Log.d(LOG_TAG, "load favorite movies");
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        return new CursorLoader(getActivity(),
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.getCount() == 0) {
            //toast no favorites message
            Toast toast = Toast.makeText(getActivity(), getResources().getText(R.string.no_favorites), Toast.LENGTH_LONG);
            toast.show();
        }
        mMovieAdapter.clear();
        mMovieList.clear();
        //data.moveToPosition(-1);
        while (data.moveToNext()) {
            //Log.d(LOG_TAG, "iterating");
            mMovieAdapter.add(new Movie(data));
        }

        if (mGridView != null && mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        //only passing back movie now
        //void onItemSelected(Uri movieUri);
        String getSortOrder();

        void onItemSelected(Movie movie);
    }

    //This task does not save in the database.
    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        final String TMDB_RESULTS = "results";
        final String TMDB_TITLE = "title";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_IMAGE_LINK = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_MOVIE_ID = "id";

        @Override
        protected void onPostExecute(Movie[] movies) {
            if (movies != null) {
                mMovieList = new ArrayList<>(Arrays.asList(movies));
                mMovieAdapter.clear();
                for (Movie m : movies) {
                    mMovieAdapter.add(m);
                }
                //mMovieAdapter.notifyDataSetChanged();
                //mMovieAdapter.addAll(mMovieList);
            }
        }

        private Movie[] getMovieDataFromJson(String jsonMovieData) throws JSONException {

            JSONObject moviesJson = new JSONObject(jsonMovieData);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            Movie[] movies = new Movie[moviesArray.length()];


            for (int i = 0; i < moviesArray.length(); i++) {

                JSONObject movieData = moviesArray.getJSONObject(i);

                movies[i] = new Movie();
                movies[i].title = movieData.getString(TMDB_TITLE);
                movies[i].originalTitle = movieData.getString(TMDB_ORIGINAL_TITLE);
                movies[i].synopsis = movieData.getString(TMDB_OVERVIEW);
                movies[i].imageLink = movieData.getString(TMDB_IMAGE_LINK);
                movies[i].userRating = movieData.getDouble(TMDB_VOTE_AVERAGE) / 2; // it's out of 10, need it out of 5
                movies[i].releaseDate = movieData.getString(TMDB_RELEASE_DATE);
                movies[i].id = movieData.getInt(TMDB_MOVIE_ID);
            }

            return movies;
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            String sortOrder = params[0];
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
                URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=" + sortOrder + "&api_key=" + UrlHelper.API_KEY);

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
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
    }
}
