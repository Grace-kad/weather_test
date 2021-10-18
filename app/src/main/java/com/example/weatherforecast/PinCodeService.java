package com.example.weatherforecast;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PinCodeService {

    @GET("pincode/{pincode}")
    Call<List<PinCodeInfo>> getPinCodeInfo(@Path("pincode") String pin);

}
