package com.example.weatherforecast;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.icu.text.CompactDecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.xmlpull.v1.XmlPullParser;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherWidget extends AppCompatActivity {

    //progressbar and scrollview that will contain the widget
    ProgressBar progress;
    ScrollView scrollView;

    //widget views
    TextView tempC, tempF, tempDesc, cityName, lon, lat;

    //input text view and input text layout to get the search icon
    TextInputLayout searchLayout;
    TextInputEditText citySearch;
    Button logout;

    //retrofit and api service
    Retrofit retrofit;
    WeatherInfoService apiService;


    //executor for network operation and avoid blocking the main thread
    ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        executor = Executors.newFixedThreadPool(3);

        progress = (ProgressBar) findViewById(R.id.progressCircle);
        scrollView = (ScrollView) findViewById(R.id.scrollView2);

        tempC = findViewById(R.id.temp);
        tempF = findViewById(R.id.temp_far);
        cityName = (TextView) findViewById(R.id.city_name);
        tempDesc = findViewById(R.id.temp_desc);
        tempDesc.setSelected(true);
        lon = findViewById(R.id.longitude);
        lat = findViewById(R.id.latitude);

        searchLayout = findViewById(R.id.city_search_layout);
        citySearch = findViewById(R.id.city_seach);
        logout = findViewById(R.id.log_out_btn);

        //initialize retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(WeatherInfoService.class);


        String name = getSharedPreferences(MainActivity.PREFERENCE_NAME,MODE_PRIVATE).getString("name","");
        Toast.makeText(getApplicationContext(),"Hello " + name,Toast.LENGTH_LONG).show();

        //handle search icon click that triggers the search
        searchLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(citySearch.getText())){
                    return;
                    //handle while the field is empty
                }else{


                    citySearch.clearFocus();

                    if(isConnected()){
                        progress.setVisibility(View.VISIBLE);
                        scrollView.setAlpha(0);
                        scrollView.setVisibility(View.GONE);
                        //execute the network task to get weather information
                        executor.execute(networkOperation);
                    }else {
                        Toast.makeText(getApplicationContext(),"check that your phone is connected to internet",Toast.LENGTH_LONG).show();
                    }


                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCE_NAME,MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("registered",false);
                editor.apply();
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
        });

    }

    //runnable for network task

    Runnable networkOperation = new Runnable() {
        @Override
        public void run() {

            String city = citySearch.getText().toString();
            String units = "metric";
            //to hide=========================================================================
            String appId = "4cfd1abbb2d765fdfecacf7165c49759";
            Call<WeatherInfo> weatherCall = apiService.getWeatherInfo(city,units,appId);

            weatherCall.enqueue(new Callback<WeatherInfo>() {
                @Override
                public void onResponse(Call<WeatherInfo> call, Response<WeatherInfo> response) {

                    if(response.isSuccessful()){

                        WeatherInfo result = response.body();

                        if(result.getCod() == 200){
                            //successful
                            int tempCel, tempFar;
                            double longitude, latitude;
                            String description, tempCity;

                            tempCel = (int) result.getMain().getTemp();
                            tempFar = ((tempCel*9)/5)+32;
                            longitude = result.getCoord().getLon();
                            latitude = result.getCoord().getLat();
                            description = result.getWeather().get(0).getDescription();
                            tempCity = result.getName();


                            tempC.setText(tempCel + "\u2103");
                            tempF.setText(tempFar + "\u2109");
                            tempDesc.setText(description);
                            cityName.setText(tempCity);
                            lon.setText(longitude + "");
                            lat.setText(latitude + "");

                            scrollView.setVisibility(View.VISIBLE);
                            scrollView.animate()
                                    .alpha(1)
                                    .setDuration(400)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            progress.setVisibility(View.GONE);
                                        }
                                    });
                        }else if(result.getCod() == 404){
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"city not found!",Toast.LENGTH_LONG).show();
                        }else if(result.getCod() == 401){
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"Access denied!",Toast.LENGTH_LONG).show();
                        }else{
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"an unexpected error occurred!",Toast.LENGTH_LONG).show();
                        }

                    }else {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"city not found!",Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(Call<WeatherInfo> call, Throwable t) {

                    progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Something went wrong!",Toast.LENGTH_LONG).show();

                }
            });

        }
    };


    //check weather the phone is connected
    boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }




}