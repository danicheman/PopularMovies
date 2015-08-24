package com.example.nick.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

public class MoviesFragment extends Fragment {

    public  static final String KEY_MOVIES_LIST = "mMovieList";
    public static final String MOVIE_BUNDLE = "movieBundle";
    final String LOG_TAG = MoviesFragment.class.getSimpleName();
    //The adapter to prepare the data for the view
    private ArrayAdapter<Movie> mMovieAdapter;
    //The array of movies retrieved from the server
    private ArrayList<Movie> mMovieList;

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
        FetchMoviesTask moviesTask = new FetchMoviesTask();

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

        if (savedInstanceState != null) {
            mMovieList = savedInstanceState.getParcelableArrayList(KEY_MOVIES_LIST);
        } else {
            mMovieList = new ArrayList<Movie>();
        }

        mMovieAdapter = new MovieAdapter(getActivity(), mMovieList);

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movieGridView);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie clickedMovie = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra(MOVIE_BUNDLE, clickedMovie);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }




}
