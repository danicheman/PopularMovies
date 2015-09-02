package com.example.nick.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.nick.popularmovies.data.MovieContract;
import com.example.nick.popularmovies.data.MovieContract.MovieReviewsEntry;
import com.example.nick.popularmovies.data.MovieContract.MovieTrailersEntry;

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

/**
 * todo: fetch genres, trailers and reviews
 * todo: save data to the database
 */
public class FetchMovieDetailsDbTask extends AsyncTask<Integer, Void, Void> {

    final String LOG_TAG = FetchMovieDetailsDbTask.class.getSimpleName();

    final String TMDB_TRAILERS = "trailers";
    final String TMDB_TRAILERS_YT = "youtube";
    final String TMDB_REVIEWS = "reviews";
    final String TMDB_RESULTS = "results";

    private final Context mContext;

    public FetchMovieDetailsDbTask(Context c) {
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

        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>(jsonTrailerArray.length());

        for (int i = 0; i < jsonTrailerArray.length(); i++) {

            ContentValues trailerValues = new ContentValues();

            JSONObject trailerData = jsonTrailerArray.getJSONObject(i);

            trailerValues.put(MovieTrailersEntry.COLUMN_KEY, trailerData.getString("key"));
            trailerValues.put(MovieTrailersEntry.COLUMN_NAME, trailerData.getString("name"));
            trailerValues.put(MovieTrailersEntry.COLUMN_MOVIE_ID, movieId);

            contentValuesArrayList.add(trailerValues);
        }

        if (contentValuesArrayList.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[contentValuesArrayList.size()];
            contentValuesArrayList.toArray(contentValuesArray);
            mContext.getContentResolver().bulkInsert(MovieContract.MovieTrailersEntry.CONTENT_URI, contentValuesArray);
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