package com.example.nick.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.nick.popularmovies.data.MovieContract.MovieEntry;

import java.util.ArrayList;

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public  static final String KEY_MOVIES_LIST = "mMovieList";
    public static final String MOVIE_BUNDLE = "movieBundle";
    //result column identifiers
    static final int COL_MOVIE_ID = 0;
    static final int COL_IMAGE_LINK = 1;
    private static final String SELECTED_KEY = "selected_position";
    private static final String[] MOVIE_COLUMNS = {
            MovieEntry._ID,
            MovieEntry.COLUMN_IMAGE_LINK
    };
    final String LOG_TAG = MoviesFragment.class.getSimpleName();
    //The adapter to prepare the data for the view
    private CursorAdapter mMovieAdapter;
    //The array of movies retrieved from the server
    private ArrayList<Movie> mMovieList;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;
    public MoviesFragment() {
        // Required empty public constructor

    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_MOVIES_LIST, mMovieList);
    }

    private void updateMovies() {
        if(UrlHelper.API_KEY == null) {
            Toast toast = Toast.makeText(getActivity(), "Please set the API KEY in the URL Helper", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //get sort order or default_movie_image value
        String sortOrder = prefs.getString("sort_order", getResources().getStringArray(R.array.sort_order_option_values)[0]);
        moviesTask.execute(sortOrder);
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

        /*if (savedInstanceState != null) {
            mMovieList = savedInstanceState.getParcelableArrayList(KEY_MOVIES_LIST);
        } else {
            mMovieList = new ArrayList<Movie>();
        }*/

        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movieGridView);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                //the callback launches the detail activity now..
                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID)));
                }
                /*Movie clickedMovie = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra(MOVIE_BUNDLE, clickedMovie);
                startActivity(intent);*/
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

        //todo: need to do something here..
        //mMovieAdapter.setUseTodayLayout(mUseTodayLayout);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.
        //todo: get this from the strings.xml
        String sortOrder = "popularity.desc";
        Uri moviesSortedUri = MovieEntry.buildMovieUri(0);
        return new CursorLoader(getActivity(),
                moviesSortedUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
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
        public void onItemSelected(Uri movieUri);
    }
}
