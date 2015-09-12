package com.example.nick.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback, MovieDetailFragment.Callback {

    //different results for the Detail Fragment when working with a favorites sort.
    public static final int RESULT_DO_NOTHING = 0;
    public static final int RESULT_REFRESH = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final int DETAIL_ACTIVITY_RESULT = 1;
    private boolean mTwoPane;
    private String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //get the sort order
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSortOrder = prefs.getString("sort_order", getResources().getStringArray(R.array.sort_order_option_values)[0]);

        if (findViewById(R.id.movie_detail_container) != null) {
            /* The detail container view will be present only in the large-screen
            layouts res/layout-sw600dp). If this view is present, the the activity
            should be in two-pane mode. */
            mTwoPane = true;
            if (savedInstanceState == null) {

                /* Fill out the right-hand-side of the two-pane view; populate the
                   detail container. */
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else mTwoPane = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //get current sort
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currentlySelectedSortOrder = prefs.getString("sort_order", getResources().getStringArray(R.array.sort_order_option_values)[0]);

        if (currentlySelectedSortOrder != null && !currentlySelectedSortOrder.equals(mSortOrder)) {

            //update both fragments with new sort order
            MoviesFragment mf = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movie_grid_view);

            /**
             * Returning from options menu to main activity with potentially new sort method,
             * Either this is tablet or phone layout, so
             * we have the movie grid, or
             * the movie grid AND the movie detail fragment.
             *
             * In the second case, let Movie Fragment populate the first movie through the
             * callback.
             */
            if (mf != null) {
                //update sort
                mf.updateMovies(currentlySelectedSortOrder);
            }
            mSortOrder = currentlySelectedSortOrder;
            MovieDetailFragment mdf = (MovieDetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);

            if (mdf != null) {
                //2 pane mode - simply reset detail pane
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public String getSortOrder() {
        if (mSortOrder == null) {
            return getResources().getString(R.string.sort_order_default);
        } else {
            return mSortOrder;
        }
    }

    @Override
    public void onItemSelected(Movie movie) {
        //todo: pass uri if loading favorite!
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.MOVIE_REFERENCE, movie);
            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, movieDetailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            //single pane..
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MoviesFragment.MOVIE_BUNDLE, movie);
            startActivityForResult(intent, DETAIL_ACTIVITY_RESULT);
        }
    }

    //refresh the grid after a favorite or unfavorite action
    @Override
    public void refreshMovieGrid() {
        if (mSortOrder.equals("favorites")) {
            MoviesFragment mf = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movie_grid_view);
            if (mf == null) {
                Log.e(LOG_TAG, "couldn't find Movie Fragment while trying to refresh grid of favorites.");
            }
            mf.updateMovies(mSortOrder);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_DO_NOTHING:
                break;
            case RESULT_REFRESH:
                MoviesFragment mf = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movie_grid_view);
                if (mf == null) {
                    Log.e(LOG_TAG, "couldn't find Movie Fragment while trying to refresh grid of favorites.");
                }
                mf.updateMovies(mSortOrder);
                break;
        }

    }
}
