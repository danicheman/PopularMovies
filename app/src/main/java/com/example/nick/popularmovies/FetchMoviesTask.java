import android.os.AsyncTask;
import android.util.Log;

import com.example.nick.popularmovies.Movie;
import com.example.nick.popularmovies.UrlHelper;

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

/**
 * todo: save data to the database instead
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

    final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    final String TMDB_RESULTS = "results";
    final String TMDB_TITLE = "title";
    final String TMDB_ORIGINAL_TITLE = "original_title";
    final String TMDB_VOTE_AVERAGE = "vote_average";
    final String TMDB_IMAGE_LINK = "poster_path";
    final String TMDB_OVERVIEW = "overview";
    final String TMDB_RELEASE_DATE = "release_date";

    @Override
    protected void onPostExecute(Movie[] movies) {
        if (movies != null) {
            mMovieList = new ArrayList<Movie>(Arrays.<Movie>asList(movies));
            mMovieAdapter.clear();
                /*for(Movie m: movies) {
                    mMovieAdapter.add(m);
                }*/
            mMovieAdapter.addAll(mMovieList);
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