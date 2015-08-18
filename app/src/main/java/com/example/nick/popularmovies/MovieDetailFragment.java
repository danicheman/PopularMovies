package com.example.nick.popularmovies;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.nick.popularmovies.Movie;
import com.example.nick.popularmovies.MoviesFragment;
import com.example.nick.popularmovies.R;
import com.example.nick.popularmovies.UrlHelper;
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

public class MovieDetailFragment extends Fragment {

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

    /**
     *
     */
    public class FetchMovieDetailsTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPostExecute(Movie[] movies) {
            if(movies != null) {
                mMovieList = new ArrayList<Movie>(Arrays.<Movie>asList(movies));
                mMovieAdapter.clear();
                for(Movie m: movies) {
                    Log.v(LOG_TAG, "got movie: " + m.title);
                    mMovieAdapter.add(m);
                }
                //mMovieAdapter.notifyDataSetChanged();
                //mMovieAdapter.addAll(mMovieList);
            }
        }

        final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();
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

            //todo: get the movie id here instead of sortorder
            String sortOrder = params[0];




            try {
                String urlString = "http://api.themoviedb.org/3/discover/movie?sort_by=" + sortOrder + "&api_key=" + UrlHelper.API_KEY;
                URL url = new URL(urlString);

                // Will contain the raw JSON response as a string.
                String moviesJsonStr = getUrlAsString(url);
                return getMovieDataFromJson(moviesJsonStr);
            } catch(MalformedURLException e) {
                Log.e(LOG_TAG, "Malformed URL" + e.getMessage());
            } catch(JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        private String getUrlAsString(URL url) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Construct the URL for the themoviedb.org query
                // Possible parameters are avaiable at TMDB's API page, at
                // http://docs.themoviedb.apiary.io/#reference/discover/discovermovie


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
                return buffer.toString();
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
        }
    }
}