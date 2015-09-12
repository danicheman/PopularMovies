package com.example.nick.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MovieDetailActivity extends AppCompatActivity implements MovieDetailFragment.Callback {

    private boolean refreshMovieGrid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new MovieDetailFragment())
                    .commit();
        }
    }


    /**
     * When you press back button on the action bar in the details activity,
     * you’ll see the state of the main activity is lost (if you had scrolled
     * in the list somewhere it just reloads). Here’s a neat trick to prevent
     * that from happening:
     */
    @Override @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Intent getParentActivityIntent() {
        // add the clear top flag - which checks if the parent (main)
        // activity is already running and avoids recreating it
        return super.getParentActivityIntent()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_movie_detail, menu);
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
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * set an activity result that tells the previous activity to refresh
     */
    @Override
    public void onBackPressed() {
        int resultCode;
        if (refreshMovieGrid) resultCode = MainActivity.RESULT_REFRESH;
        else resultCode = MainActivity.RESULT_DO_NOTHING;
        setResult(resultCode);
        super.onBackPressed();
    }


    //Callback for detail fragment when adding or removing a movie from favorites.
    @Override
    public void refreshMovieGrid() {
        refreshMovieGrid = true;
    }
}
