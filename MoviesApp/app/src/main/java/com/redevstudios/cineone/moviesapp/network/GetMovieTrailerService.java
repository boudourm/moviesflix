package com.redevstudios.cineone.moviesapp.network;

import com.redevstudios.cineone.moviesapp.model.MovieTrailerPageResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetMovieTrailerService {
    @GET("movie/{id}/videos")
    Call<MovieTrailerPageResult> getTrailers(@Path("id") int movieId, @Query("api_key") String userkey);
}
