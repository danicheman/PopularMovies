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
    static final String TEST_GENRE = "action";
    static final String TEST_MOVIE = "Terminator";


    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static ContentValues createMovieValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_TITLE, TEST_MOVIE);
        testValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, "Arnold as a machine with an accent, kicking ass");
        testValues.put(MovieContract.MovieEntry.COLUMN_RATING, 86);
        testValues.put(MovieContract.MovieEntry.COLUMN_IMAGE_LINK, "http://www.example.com/sample.jpg");
        //IS_FAVORITE will naturally default to zero so don't set it here

        return testValues;
    }

    static ContentValues createGenreValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.GenreEntry.COLUMN_TITLE, TEST_GENRE);
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
    static long insertGenreValues(Context context) {
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createGenreValues();

        long genreRowId = db.insert(MovieContract.GenreEntry.TABLE_NAME, null, testValues);

        assertTrue("Error: Failure to insert Genre Action", genreRowId != -1);

        return genreRowId;
    }

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
