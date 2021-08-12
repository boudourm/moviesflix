package com.bb.movies_application.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bb.movies_application.R;
import com.bb.movies_application.model.Movie;
import com.bb.movies_application.model.MovieResult;
import com.bb.movies_application.network.GetMovieDataService;
import com.bb.movies_application.network.RetrofitInstance;
import com.bb.movies_application.ui.adapter.MovieAdapter;
import com.bb.movies_application.ui.data.FavoriteContract;
import com.bb.movies_application.ui.utils.EndlessRecyclerViewScrollListener;
import com.bb.movies_application.ui.utils.MovieClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final String API_KEY = "1abe855bc465dce9287da07b08a664eb";
    private static final int FIRST_PAGE = 1;
    private static int totalPages;
    public static int currentList = 1;
    private Call<MovieResult> call;
    private List<Movie> movieResults;
    private MovieAdapter movieAdapter;

    @BindView(R.id.rv_movies) RecyclerView recyclerView;
    @BindView(R.id.tv_no_internet_error)
    ConstraintLayout mNoInternetMessage;
    @BindView(R.id.movie_activity_title) TextView mpageTitle;
    @BindView(R.id.no_search_results) TextView msearchresults;
    SearchView mySearchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

       Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!isNetworkAvailable()){
            recyclerView.setVisibility(View.GONE);
            mNoInternetMessage.setVisibility(View.VISIBLE);
        }

        recyclerView = findViewById(R.id.rv_movies);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        recyclerView.setLayoutManager(manager);

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d("Load More   : ", "onLoadMore: ");
                if ((page + 1) <= totalPages && currentList != 3) {
                    loadPage(page + 1);
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        movieAdapter = new MovieAdapter(new ArrayList<>(), new MovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                Intent intent = new Intent(MainActivity.this, MovieActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("movie", movie);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(movieAdapter);

        loadPage(FIRST_PAGE);
        mySearchView = findViewById(R.id.searchView);
        //Search
        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerView.setVisibility(View.VISIBLE);
                searchMovie(newText);
                return false;
            }
        });
        mySearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d("Closee    :", "onClose: ");
                recyclerView.setVisibility(View.VISIBLE);
                loadPage(FIRST_PAGE);
                return true;
            }
        });


    }

    private void searchMovie(String newText) {

        if(this.isNetworkAvailable()) mpageTitle.setText(R.string.search_results);
        GetMovieDataService movieDataService = RetrofitInstance.getRetrofitInstance().create(GetMovieDataService.class);
                call = movieDataService.getMovie(newText , API_KEY);

        call.enqueue(new Callback<MovieResult>() {
            @Override
            public void onResponse(@NonNull Call<MovieResult> call, @NonNull  Response<MovieResult> response) {
                movieResults = new ArrayList<>();
                if(response!=null) {
                    assert response.body() != null;
                    if(response.body() != null) {
                        movieResults = response.body().getMovieResult();
                        if (movieResults.size() == 0) {
                            recyclerView.setVisibility(View.GONE);
                            int visi = recyclerView.getVisibility();
                            Log.d("RecyclerView", "visibility   " + visi);
                            msearchresults.setText(R.string.no_search_results);
                        } else {

                            Log.d("list size   ", " " + movieResults.size());
                            assert response.body() != null;
                            totalPages = response.body().getTotalPages();
                            movieAdapter.setMovieList(new ArrayList<>());
                            movieAdapter.addMoviesList(movieResults);
                        }
                    }
                }

            }


            @Override
            public void onFailure(Call<MovieResult> call, Throwable t) {
                //Toast.makeText(MainActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                Log.d("Failed  : ", "onFailure: ");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        mySearchView.clearFocus();
        recyclerView.setVisibility(View.VISIBLE);
        switch (item.getItemId()) {
            case R.id.logo:
                currentList = 1;
                break;
            case R.id.sort_by_popularity:
                currentList = 1;
                break;
            case R.id.sort_by_top:
                currentList = 2;
                break;
            case R.id.sort_by_upcoming:
                currentList = 4;
                break;
            case R.id.sort_by_favorites:
                currentList = 3;
                break;
        }
        if(currentList != 3){
            loadPage(FIRST_PAGE);
        } else {
            if(this.isNetworkAvailable()) updatePageTitle();
            ArrayList<Movie> favoriteMovies = getFavoriteMovies();

            movieAdapter = new MovieAdapter(favoriteMovies, new MovieClickListener() {
                @Override
                public void onMovieClick(Movie movie) {
                    Intent intent = new Intent(MainActivity.this, MovieActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("movie", movie);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(movieAdapter);
        }

        return super.onOptionsItemSelected(item);

    }


    public void loadPage(final int page) {

        if(this.isNetworkAvailable()) updatePageTitle();
        GetMovieDataService movieDataService = RetrofitInstance.getRetrofitInstance().create(GetMovieDataService.class);
        switch(currentList){
            case 1:
                call = movieDataService.getPopularMovies(page, API_KEY);
                break;
            case 2:
                call = movieDataService.getTopRatedMovies(page, API_KEY);
                break;
            case 4:
                call = movieDataService.getUpcomingMovies(page, API_KEY);
                break;
        }

        call.enqueue(new Callback<MovieResult>() {
            @Override
            public void onResponse(@NonNull Call<MovieResult> call, @NonNull  Response<MovieResult> response) {

                if(page == 1) {
                    movieResults = new ArrayList<>();
                    assert response.body() != null;
                    movieResults = response.body().getMovieResult();
                    assert response.body() != null;
                    totalPages = response.body().getTotalPages();
                    movieAdapter.setMovieList( new ArrayList<>());
                    movieAdapter.addMoviesList(movieResults);

                } else {
                    /*
                    assert response.body() != null;
                    List<Movie> movies = response.body().getMovieResult();
                    for(Movie movie : movies){
                        movieResults.add(movie);
                        movieAdapter.notifyItemInserted(movieResults.size() - 1);
                        */
                    assert response.body() != null;
                    movieResults = response.body().getMovieResult();
                    assert response.body() != null;
                    totalPages = response.body().getTotalPages();
                    movieAdapter.addMoviesList(movieResults);
                    movieAdapter.notifyItemInserted(movieResults.size() - 1);
                    }
                }


            @Override
            public void onFailure(Call<MovieResult> call, Throwable t) {
                //Toast.makeText(MainActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                Log.d("Failed  : ", "onFailure: ");
            }
        });
    }

    private void updatePageTitle(){
        if(currentList==1)
            mpageTitle.setText(R.string.popular_movies);
        if(currentList==2)
            mpageTitle.setText(R.string.top_rated_movies);
        if(currentList==3)
            mpageTitle.setText(R.string.favorite_movies);
        if (currentList == 4)
            mpageTitle.setText(R.string.upcoming_movies);

    }

    public static String movieImagePathBuilder(String imagePath) {
        return "https://image.tmdb.org/t/p/" +
                "w500" +
                imagePath;
    }


    private ArrayList<Movie> getFavoriteMovies(){
        ArrayList<Movie> movieList = new ArrayList<>();
        Cursor cursor = getContentResolver()
                .query(FavoriteContract.FavoriteEntry.CONTENT_URI,null,null,null,null);

        //assert cursor != null;
        if(cursor!=null) {
            if (cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie();

                    int id = cursor.getInt(cursor.getColumnIndex("movie_id"));
                    String movieTitle = cursor.getString(cursor.getColumnIndex("movie_title"));
                    String movieOverview = cursor.getString(cursor.getColumnIndex("movie_overview"));
                    double movieVoteAverage = cursor.getDouble(cursor.getColumnIndex("movie_vote_average"));
                    String movieReleaseDate = cursor.getString(cursor.getColumnIndex("movie_release_date"));
                    String moviePosterPath = cursor.getString(cursor.getColumnIndex("movie_poster_path"));

                    movie.setId(id);
                    movie.setTitle(movieTitle);
                    movie.setOverview(movieOverview);
                    movie.setVoteAverage(movieVoteAverage);
                    movie.setReleaseDate(movieReleaseDate);
                    movie.setPosterPath(moviePosterPath);

                    movieList.add(movie);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }


        return movieList;
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();


    }

    @OnClick(R.id.tv_no_internet_error_refresh)
    public void refreshActivity(){
        finish();
        startActivity(getIntent());
    }

}