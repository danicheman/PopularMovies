package com.example.nick.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);*/

        if(findViewById(R.id.movie_detail_container) != null) {
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
        } else {
            mTwoPane = false;

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //todo: get selected movie id

        //todo: find fragment by tag

        //todo: on movie changed void method

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
    public void onItemSelected(Uri movieUri) {
        Intent intent = new Intent(this, MovieDetailActivity.class)
                .setData(movieUri);
        startActivity(intent);
    }
}
