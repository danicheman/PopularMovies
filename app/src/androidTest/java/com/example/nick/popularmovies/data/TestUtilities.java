package com.example.nick.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by NICK on 8/18/2015.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_KEY = "abc123";
    static final String TEST_NAME = "trailer 1";
    static final String TEST_MOVIE = "Terminator";
    static final String REVIEW_REVIEW = "TERMINATOR 1 ROOOOCKS";
    static final String REVIEW_LINK = "http://www.myreviews.com/terminator_review";
    static final String REVIEW_ID = "12341abcd";
    static final String REVIEW_AUTHOR = "Nick O";


    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static ContentValues createMovieValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_TITLE, TEST_MOVIE);
        testValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, "Arnold as a machine with an accent, kicking ass");
        testValues.put(MovieContract.MovieEntry.COLUMN_RATING, 9);
        testValues.put(MovieContract.MovieEntry.COLUMN_IMAGE_LINK, "http://www.example.com/sample.jpg");
        testValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "12/8/1986");
        //IS_FAVORITE will naturally default to zero so don't set it here

        return testValues;
    }

    static ContentValues createTrailerValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieTrailersEntry.COLUMN_KEY, TEST_KEY);
        testValues.put(MovieContract.MovieTrailersEntry.COLUMN_NAME, TEST_NAME);
        return testValues;
    }

    static ContentValues createReviewValues() {

        //cannot add a review without a valid movie id.
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieReviewsEntry.COLUMN_REVIEW, REVIEW_REVIEW);
        testValues.put(MovieContract.MovieReviewsEntry.COLUMN_REVIEW_LINK, REVIEW_LINK);
        testValues.put(MovieContract.MovieReviewsEntry.COLUMN_REVIEW_ID, REVIEW_ID);
        testValues.put(MovieContract.MovieReviewsEntry.COLUMN_AUTHOR, REVIEW_AUTHOR);
        return testValues;
    }

    static long insertTerminatorMovieValues(Context context) {

        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createMovieValues();

        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert Terminator Movie Values", movieRowId != -1);

        return movieRowId;

    }

    //To be called by test provider
    /*static long insertGenreValues(Context context) {
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createGenreValues();

        long genreRowId = db.insert(MovieContract.GenreEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert Genre Action", genreRowId != -1);

        return genreRowId;
    }*/

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
