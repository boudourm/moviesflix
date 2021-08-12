package com.bb.movies_application.network;

import com.bb.movies_application.model.Movie;
import com.bb.movies_application.model.MovieResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

@SuppressWarnings("ALL")
public interface GetMovieDataService {

    @GET("movie/popular")
    Call<MovieResult> getPopularMovies(@Query("page") int page, @Query("api_key") String userkey);

    @GET("movie/top_rated")
    Call<MovieResult> getTopRatedMovies(@Query("page") int page, @Query("api_key") String userkey);

    @GET("movie/upcoming")
    Call<MovieResult> getUpcomingMovies(@Query("page") int page, @Query("api_key") String userkey);

    @GET("search/movie")
    Call<MovieResult> getMovie(@Query("query") String query, @Query("api_key") String userkey);

    @GET("movie/{id}")
    Call<Movie> getMovieById(@Path("id") int movieId, @Query("api_key") String userkey);
}
