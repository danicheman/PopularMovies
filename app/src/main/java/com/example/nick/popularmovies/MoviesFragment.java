package com.example.nick.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.view.View.OnClickListener;

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
import java.util.List;



public class MoviesFragment extends Fragment {

    final String LOG_TAG = MoviesFragment.class.getSimpleName();
    public static final String MOVIE_BUNDLE = "movieBundle";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private ArrayAdapter<Movie> mMovieAdapter;

    public MoviesFragment() {
        // Required empty public constructor

    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        //todo: load sharedprefs here!
        moviesTask.execute("sort order goes in here");
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "Launching view of Fragment");
        mMovieAdapter = new ArrayAdapter<Movie>(
                getActivity(),
                R.layout.fragment_movies,
                R.id.movieGridView,
                new ArrayList<Movie>());
        mMovieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

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

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {
        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            mMovieAdapter.clear();

            for (Movie m : movies) {
                //Log.v(LOG_TAG, "recieved movie:"+ m.title);
                mMovieAdapter.add(m);
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
                movies[i].userRating = movieData.getDouble(TMDB_VOTE_AVERAGE) / 2; // it's out of 10
                movies[i].releaseDate = movieData.getString(TMDB_RELEASE_DATE);
            }

            return movies;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {


                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String moviesJsonStr = null;

                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=b8a4068d1466dca29becff1029e0e0e1");

                    // Create the request to OpenWeatherMap, and open the connection
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
                    Log.e("PlaceholderFragment", "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
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
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }

                try {
                    Movie[] movieArray = getMovieDataFromJson(moviesJsonStr);
                    return new ArrayList(Arrays.asList(movieArray));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            return null;
        }
    }


}
