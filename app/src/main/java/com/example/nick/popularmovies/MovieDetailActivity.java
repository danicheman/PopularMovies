package com.example.nick.popularmovies;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_movie_detail, new MovieDetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
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

    public static class MovieDetailFragment extends Fragment {

        public MovieDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for movie data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(MoviesFragment.MOVIE_BUNDLE)) {
                Movie m = (Movie) intent.getSerializableExtra(MoviesFragment.MOVIE_BUNDLE);

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
    }
}
