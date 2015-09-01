package com.example.nick.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MovieProvider extends ContentProvider {

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int TRAILER_WITH_MOVIE_ID = 200;
    static final int TRAILER = 201;
    static final int REVIEW_WITH_MOVIE_ID = 300;
    static final int REVIEW = 301;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;


    public MovieProvider() {
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILER + "/#", TRAILER_WITH_MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_REVIEW + "/#", REVIEW_WITH_MOVIE_ID);
        //match different URIs here
        return matcher;
    }

    //id = ?
    private final Cursor getMovieById(Uri uri, String[] projection) {
        String movieId = MovieContract.MovieEntry.getMovieIdFromUri(uri);

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);

        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                MovieContract.MovieEntry._ID + " = ?",
                new String[]{movieId}, null, null, null);
    }

    //id = ?
    private final Cursor getReviewsByMovieId(Uri uri, String[] projection) {

        String movieId = MovieContract.MovieReviewsEntry.getMovieIdFromUri(uri);

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(MovieContract.MovieReviewsEntry.TABLE_NAME);


        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                MovieContract.MovieReviewsEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movieId}, null, null, null);
    }

    private final Cursor getTrailersByMovieId(Uri uri, String[] projection) {
        String movieId = MovieContract.MovieTrailersEntry.getMovieIdFromUri(uri);

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(MovieContract.MovieTrailersEntry.TABLE_NAME);

        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                MovieContract.MovieTrailersEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movieId}, null, null, null);
    }

    private final Cursor getFavoriteMovies(Uri uri, String[] projection) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);

        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection, null, null, null, null, null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(
                        MovieContract.MovieTrailersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(
                        MovieContract.MovieReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case REVIEW: //for bulk inserting
                return MovieContract.MovieReviewsEntry.CONTENT_TYPE;
            case REVIEW_WITH_MOVIE_ID:
                return MovieContract.MovieReviewsEntry.CONTENT_ITEM_TYPE;
            case TRAILER:// for bulk inserting
                return MovieContract.MovieTrailersEntry.CONTENT_TYPE;
            case TRAILER_WITH_MOVIE_ID:
                return MovieContract.MovieTrailersEntry.CONTENT_ITEM_TYPE;

        }
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                //normalizeDate(values);
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case REVIEW: {
                long _id = db.insert(MovieContract.MovieReviewsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieReviewsEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case TRAILER: {
                long _id = db.insert(MovieContract.MovieTrailersEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieTrailersEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor resultCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_WITH_ID:
                resultCursor = getMovieById(uri, projection);
                break;
            case TRAILER_WITH_MOVIE_ID:
                resultCursor = getTrailersByMovieId(uri, projection);
                break;
            case REVIEW_WITH_MOVIE_ID:
                resultCursor = getReviewsByMovieId(uri, projection);
                break;
            case MOVIE: //just for favorites
                resultCursor = getFavoriteMovies(uri, projection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return resultCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String dbTable;

        switch (match) {
            case MOVIE:
                dbTable = MovieContract.MovieEntry.TABLE_NAME;
                break;
            case REVIEW_WITH_MOVIE_ID:
                dbTable = MovieContract.MovieReviewsEntry.TABLE_NAME;
                break;
            case TRAILER_WITH_MOVIE_ID:
                dbTable = MovieContract.MovieTrailersEntry.TABLE_NAME;
                break;
            default:
                return super.bulkInsert(uri, values);
        }

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {

                long _id = db.insert(dbTable, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }
}
