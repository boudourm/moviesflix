package com.bb.movies_application.ui.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bb.movies_application.R;
import com.bb.movies_application.model.Movie;
import com.bb.movies_application.model.MovieResult;
import com.bb.movies_application.model.MovieTrailer;
import com.bb.movies_application.model.MovieTrailerResult;
import com.bb.movies_application.network.GetMovieDataService;
import com.bb.movies_application.network.GetMovieTrailerService;
import com.bb.movies_application.network.RetrofitInstance;
import com.bb.movies_application.ui.adapter.TrailerAdapter;
import com.bb.movies_application.ui.data.FavoriteContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

import static com.bb.movies_application.ui.activity.MainActivity.API_KEY;
import static com.bb.movies_application.ui.activity.MainActivity.movieImagePathBuilder;
import static java.lang.Thread.sleep;

@SuppressWarnings("ALL")
public class MovieActivity extends AppCompatActivity {
    @BindView(R.id.movie_activity_title) TextView mMovieTitle;
    @BindView(R.id.detail_movie_poster) ImageView mMoviePoster;
    @BindView(R.id.detail_movie_poster_flipped) ImageView mMoviePosterReflection;
    @BindView(R.id.detail_movie_cover) ImageView mMovieCover;
    @BindView(R.id.movie_activity_overview) TextView mMovieOverview;
    @BindView(R.id.movie_activity_public) TextView mMoviePublic;
    @BindView(R.id.movie_activity_language) TextView mMovieLaguage;
    @BindView(R.id.movie_activity_release_date) TextView mMovieReleaseDate;
    @BindView(R.id.movie_activity_rating) TextView mMovieRating;
    @BindView(R.id.movie_activity_favorite) FloatingActionButton mFavoriteButton;
    @BindView(R.id.rv_movie_trailers) FloatingActionButton mTrailerPlayButton;
   // @BindView(R.id.movie_activity_trailer_label) TextView mMovieTrailerLabel;
  //  @BindView(R.id.movie_activity_read_reviews) TextView mReviewsLabel;

    //@BindView(R.id.rv_movie_trailers) RecyclerView mTrailerRecyclerView;

    private TrailerAdapter mTrailerAdapter;
    private ArrayList<MovieTrailer> mMovieTrailers;
    private MovieTrailer choosenTrailer;
    private String MovieIMDB;



    private Movie mMovie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie);

        ButterKnife.bind(this);
//To Delete
      //  mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
       // mTrailerRecyclerView.setNestedScrollingEnabled(false);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            mMovie = (Movie) bundle.getSerializable("movie");
            getMovie(mMovie.getId());
            Log.d("IMDB", "IMDB " + mMovie.getImdb_id());
            populateActivity(mMovie);
            if(isNetworkAvailable()){
                getTrailers(mMovie.getId());
            }
        } else{
            mMovie = (Movie) savedInstanceState.getSerializable("movie");
            getMovie(mMovie.getId());
            Log.d("IMDB 1", "IMDB " + mMovie.getImdb_id());
            populateActivity(mMovie);

            if(isNetworkAvailable()){
                mMovieTrailers = (ArrayList<MovieTrailer>) savedInstanceState.getSerializable("movie_trailers");
                this.choosenTrailer = mMovieTrailers.get(0);
                //populateTrailers(mMovieTrailers);
            }
        }
    }

    private void getMovie(int movieId) {
        GetMovieDataService movieDataService = RetrofitInstance.getRetrofitInstance().create(GetMovieDataService.class);
        Call<Movie> call = movieDataService.getMovieById(movieId, API_KEY);

        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                mMovie.setImdb_id(response.body().getImdb_id());
                Log.d("MOVIEIMDB  set ", "onResponse: " + MovieIMDB);
                MovieIMDB = response.body().getImdb_id() ;
                Log.d("MOVIEIMDB  ", "onResponse: " + MovieIMDB);
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Toast.makeText(MovieActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void populateActivity(Movie mMovie){
        updateFabDrawable();
        Picasso.with(this).load(movieImagePathBuilder(mMovie.getPosterPath())).into(mMoviePoster);
        Picasso.with(this).load(movieImagePathBuilder(mMovie.getPosterPath())).into(mMoviePosterReflection);
        Picasso.with(this).load(movieImagePathBuilder(mMovie.getBackdropPath())).into(mMovieCover);
        mMovieTitle.setText(mMovie.getTitle());
        mMovieOverview.setText(mMovie.getOverview());
        mMovieReleaseDate.setText(mMovie.getReleaseDate());
        mMovieLaguage.setText(mMovie.getOriginalLanguage());

        if(mMovie.isAdult())
        {
            mMoviePublic.setText("Adults");
        }
        else
        {
            mMoviePublic.setText("All");
        }

        MovieIMDB = mMovie.getImdb_id();
        System.out.println("MOvie Homepage " + MovieIMDB);

       if(MovieIMDB == null)
        {
            View button = (View) findViewById(R.id.homepage_button);
            button.setVisibility(View.GONE);
        }
        String userRatingText = String.valueOf(mMovie.getVoteAverage()) + "/10 ★";
        mMovieRating.setText(userRatingText);
    }


    public void onClickWatchTrailer(View view) {
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + choosenTrailer.getKey()));
        startActivity(intent);
    }

    public void onClickOfficialWebSite(View view) {
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.imdb.com/title/"+MovieIMDB));
        startActivity(intent);
    }


//ToDelete
 /*  private void populateTrailers(ArrayList<MovieTrailer> mMovieTrailers){
        if(mMovieTrailers.size() > 0){
            //To Delete
            mMovieTrailerLabel.setVisibility(View.VISIBLE);
            mTrailerRecyclerView.setVisibility(View.VISIBLE);
            mTrailerAdapter = new TrailerAdapter(mMovieTrailers, new TrailerClickListener() {
                @Override
                public void onMovieTrailerClick(MovieTrailer mMovieTrailer) {
                    Intent mTrailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + mMovieTrailer.getKey()));
                    startActivity(mTrailerIntent);
                }
            });
            mTrailerRecyclerView.setAdapter(mTrailerAdapter);
        }
    }*/


    private void getTrailers(int movieId) {
        GetMovieTrailerService movieTrailerService = RetrofitInstance.getRetrofitInstance().create(GetMovieTrailerService.class);
        Call<MovieTrailerResult> call = movieTrailerService.getTrailers(movieId, API_KEY);


        call.enqueue(new Callback<MovieTrailerResult>() {
            @Override
            public void onResponse(Call<MovieTrailerResult> call, Response<MovieTrailerResult> response) {
                mMovieTrailers = response.body().getTrailerResult();
                choosenTrailer = mMovieTrailers.get(0);


               // populateTrailers(mMovieTrailers);
            }

            @Override
            public void onFailure(Call<MovieTrailerResult> call, Throwable t) {
                Toast.makeText(MovieActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.movie_activity_favorite)
    public void setFavoriteMovie(){
        ContentValues values = new ContentValues();
        values.put(FavoriteContract.FavoriteEntry.MOVIE_ID, mMovie.getId());
        values.put(FavoriteContract.FavoriteEntry.MOVIE_TITLE, mMovie.getTitle());
        values.put(FavoriteContract.FavoriteEntry.MOVIE_OVERVIEW, mMovie.getOverview());
        values.put(FavoriteContract.FavoriteEntry.MOVIE_VOTE_COUNT, mMovie.getVoteCount());
        values.put(FavoriteContract.FavoriteEntry.MOVIE_VOTE_AVERAGE, mMovie.getVoteAverage());
        values.put(FavoriteContract.FavoriteEntry.MOVIE_RELEASE_DATE, mMovie.getReleaseDate());
        values.put(FavoriteContract.FavoriteEntry.MOVIE_POSTER_PATH, mMovie.getPosterPath());

        if(!isFavorited(mMovie.getId())){
            getContentResolver().
                    insert(FavoriteContract.FavoriteEntry.CONTENT_URI, values);
            Toast.makeText(this, R.string.movie_added_to_favorites, Toast.LENGTH_SHORT).show();
        }else{
            getContentResolver().delete(FavoriteContract.FavoriteEntry.CONTENT_URI,
                    "movie_id=?",
                    new String[]{String.valueOf(mMovie.getId())});
            Toast.makeText(this, R.string.movie_removed_from_favorites, Toast.LENGTH_SHORT).show();
        }

        updateFabDrawable();


    }

    private boolean isFavorited(int id){
        Cursor cursor = getContentResolver()
                .query(FavoriteContract.FavoriteEntry.buildFavoriteUriWithId(id),null,null,null,null);
        if(cursor != null)
        return cursor.getCount()> 0;
        else return false;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void updateFabDrawable(){
        if(isFavorited(mMovie.getId())){
            mFavoriteButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white_36dp));
        } else{
            mFavoriteButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_border_white_36dp));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("movie", mMovie);
        if(isNetworkAvailable()){
            outState.putSerializable("movie_trailers", mMovieTrailers);
        }
    }
}
