package com.example.weatherforecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherInfoService {

    @GET("data/2.5/weather/")
    Call<WeatherInfo> getWeatherInfo(@Query("q") String city, @Query("units") String units, @Query("appid") String api_key);

}
