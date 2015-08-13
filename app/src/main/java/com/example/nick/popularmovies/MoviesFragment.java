package com.example.nick.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

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

public class MoviesFragment extends Fragment {

    final String LOG_TAG = MoviesFragment.class.getSimpleName();
    public  static final String KEY_MOVIES_LIST = "mMovieList";
    public static final String MOVIE_BUNDLE = "movieBundle";

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

    /**
     *
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPostExecute(Movie[] movies) {
            if(movies != null) {
                mMovieList = new ArrayList<Movie>(Arrays.<Movie>asList(movies));
                mMovieAdapter.clear();
                /*for(Movie m: movies) {
                    mMovieAdapter.add(m);
                }*/
                mMovieAdapter.addAll(mMovieList);
            }
        }

        final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        final String TMDB_RESULTS = "results";
        final String TMDB_TITLE = "title";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_IMAGE_LINK = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";


        private Movie[] getMovieDataFromJson(String jsonMovieData) throws JSONException {

            JSONObject moviesJson = new JSONObject(jsonMovieData);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            Movie[] movies = new Movie[moviesArray.length()];



            for(int i = 0; i < moviesArray.length(); i++) {

                JSONObject movieData = moviesArray.getJSONObject(i);

                movies[i] = new Movie();
                movies[i].title = movieData.getString(TMDB_TITLE);
                movies[i].originalTitle = movieData.getString(TMDB_ORIGINAL_TITLE);
                movies[i].synopsis = movieData.getString(TMDB_OVERVIEW);
                movies[i].imageLink = movieData.getString(TMDB_IMAGE_LINK);
                movies[i].userRating = movieData.getDouble(TMDB_VOTE_AVERAGE) / 2; // it's out of 10, need it out of 5
                movies[i].releaseDate = movieData.getString(TMDB_RELEASE_DATE);
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
                URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by="+sortOrder+"&api_key="+UrlHelper.API_KEY);

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
            } finally{
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
