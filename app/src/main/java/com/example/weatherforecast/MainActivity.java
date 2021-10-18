package com.example.weatherforecast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.xmlpull.v1.XmlPullParser;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    //define variable========================================

    TextInputLayout birthLayout, pinInputLayout;


    TextInputEditText phone, name, birthDate,addr1, addr2, pin, district, state;
    RadioButton male, female;
    RadioGroup gender;
    Button checkPin, register;

    //shared preference name
    static public String PREFERENCE_NAME = "USER_DATA";

    TextView errorMsg, pinErrMsg;

    DatePickerDialog.OnDateSetListener dateSetListener;


    Retrofit retrofit;
    PinCodeService pinCodeService;

    //executor for network operation
    ExecutorService executor;


    //permission to check network info
    ActivityResultLauncher<String> permissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSharedPreferences(PREFERENCE_NAME,MODE_PRIVATE).getBoolean("registered",false)){
            Intent intent = new Intent(getApplicationContext(), WeatherWidget.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);


        //network permission
        permissionRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
           if(granted){
               Toast.makeText(getApplicationContext(),"Permission granted!",Toast.LENGTH_LONG).show();
           }
        });

        //initialize variables========================================

        phone = (TextInputEditText) findViewById(R.id.phone_number);
        name = (TextInputEditText) findViewById(R.id.full_name);
        birthDate = (TextInputEditText) findViewById(R.id.birth);
        birthLayout = (TextInputLayout) findViewById(R.id.birth_date_layout);
        pinInputLayout = (TextInputLayout) findViewById(R.id.pin_input_layout);
        addr1 = (TextInputEditText) findViewById(R.id.address_line_1);
        addr2 = (TextInputEditText) findViewById(R.id.address_line_2);
        pin = (TextInputEditText) findViewById(R.id.pin_code);
        district = (TextInputEditText) findViewById(R.id.district);
        state = (TextInputEditText) findViewById(R.id.state);

        executor = Executors.newSingleThreadExecutor();

        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        gender = findViewById(R.id.gender);

        checkPin = (Button) findViewById(R.id.check_pin);
        register= (Button) findViewById(R.id.submit);

        errorMsg = (TextView) findViewById(R.id.error_msg);
        pinErrMsg = (TextView) findViewById(R.id.pin_error_mdg);


        //check focus to avoid pin change after setting district and state===========
        pin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    district.getText().clear();
                    state.getText().clear();
                }
            }
        });


        //date set listener=============================================================
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                String date = i2 + "/" + i1 + "/" + i;
                birthDate.setText(date);
                if(birthDate.hasFocus()){
                    birthDate.clearFocus();
                }
            }
        };


        //Retrofit initialization for pin code check====================================

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.postalpincode.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        pinCodeService = retrofit.create(PinCodeService.class);





        //show date picker dialog=======================================================

        birthLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                //date end Icon clicked
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        dateSetListener,
                        year,month,day);
                datePicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePicker.show();
            }
        });




        //check pin and extract state and district========================================

        checkPin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                //check if network permission is granted
                if(checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
                    permissionRequest.launch(Manifest.permission.ACCESS_NETWORK_STATE);
                }

                //check if the pi code field is empty

                if(TextUtils.isEmpty(pin.getText())){
                    pin.requestFocus();
                    pinErrMsg.setText("Enter your pin code");
                    pinErrMsg.setVisibility(View.VISIBLE);
                }else {

                    if(isConnected()){

                        Drawable loadingIcon = getDrawable();
                        pinInputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                        pinInputLayout.setEndIconDrawable(loadingIcon);
                        Animatable animatedDrawable = (Animatable) pinInputLayout.getEndIconDrawable();
                        animatedDrawable.start();
                        executor.execute(pinCodeCheckRunnable);

                    }else {
                        pinErrMsg.setText("Couldn't fetch pin code information!! \n Make sure you have an active internet connection");
                        pinErrMsg.setVisibility(View.VISIBLE);
                    }



                }
            }
        });


        //register new user==================================================================

        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String f_name, f_phone, f_dob, f_gender, f_pin, f_state, f_district, f_addr1, f_addr2, errors = "";
                Boolean flag = true;


                //shared preference to save data
                SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if(!TextUtils.isEmpty(phone.getText())){
                    f_phone = phone.getText().toString();
                    editor.putString("phone",f_phone);
                }
                else{
                    errors += "Phone is required \n";
                    flag = false;
                    phone.requestFocus();
                }

                if(!TextUtils.isEmpty(name.getText())){
                    f_name = name.getText().toString();
                    editor.putString("name",f_name);
                }
                else{
                    errors += "Name is required \n";
                    flag = false;
                    name.requestFocus();
                }


                if(!TextUtils.isEmpty(birthDate.getText())){
                    f_dob = birthDate.getText().toString();
                    editor.putString("birthDate",f_dob);
                }
                else{
                    errors += "Birth date is required \n";
                    flag = false;
                    birthDate.requestFocus();
                }


                if(male.isChecked()){
                    f_gender = "male";
                    editor.putString("gender",f_gender);
                }else if(female.isChecked()){
                    f_gender = "female";
                    editor.putString("gender",f_gender);
                }else if(!male.isChecked() && !female.isChecked()){
                    errors += "Gender is required";
                    flag = false;
                }

                if(!TextUtils.isEmpty(addr1.getText())){
                    f_addr1= addr1.getText().toString();
                    editor.putString("addressLine1",f_addr1);
                }
                else{
                    errors += "Address is required \n";
                    flag = false;
                    addr1.requestFocus();
                }

                if(!TextUtils.isEmpty(addr2.getText())){
                    f_addr2= addr2.getText().toString();
                    editor.putString("addressLine2",f_addr2);
                }


                if(!TextUtils.isEmpty(pin.getText())){
                    f_pin= pin.getText().toString();
                    editor.putString("pinCode",f_pin);
                }
                else{
                    errors += "Pin code is required \n";
                    flag = false;
                    pin.requestFocus();
                }


                if(!TextUtils.isEmpty(district.getText())){
                    f_district= district.getText().toString();
                    editor.putString("district",f_district);
                }
                else{
                    errors += "District is required \n";
                    flag = false;
                    district.requestFocus();
                }


                if(!TextUtils.isEmpty(state.getText())){
                    f_state = state.getText().toString();
                    editor.putString("state",f_state);
                }
                else{
                    errors += "State is required \n";
                    flag = false;
                    state.requestFocus();
                }


                if(flag){
                    errorMsg.setVisibility(View.GONE);
                    editor.putBoolean("registered",true);
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(), WeatherWidget.class);
                    startActivity(intent);
                }else{
                    errorMsg.setText(errors);
                    errorMsg.setVisibility(View.VISIBLE);
                }

            }
        });



    }

    Runnable pinCodeCheckRunnable = new Runnable() {
        @Override
        public void run() {
            Call<List<PinCodeInfo>> pinInfoCall = pinCodeService.getPinCodeInfo(pin.getText().toString());
            pinInfoCall.enqueue(new Callback<List<PinCodeInfo>>() {
                @Override
                public void onResponse(Call<List<PinCodeInfo>> call, Response<List<PinCodeInfo>> response) {

                    if(response.isSuccessful()){
                        List<PinCodeInfo> pinResponse = response.body();
                        PinCodeInfo info = pinResponse.get(0);
                        if(TextUtils.equals(info.getStatus(),"Success")){
                            PostOffice postOffice = info.getPostOffice().get(0);
                            district.setText(postOffice.getDistrict());
                            state.setText(postOffice.getState());
                            pin.clearFocus();
                            pinErrMsg.setVisibility(View.GONE);

                            //change end icon
                            Animatable loadingIcon = (Animatable) pinInputLayout.getEndIconDrawable();
                            loadingIcon.stop();
                            pinInputLayout.setEndIconDrawable(R.drawable.ic_check);

                        }else{
                            pinErrMsg.setText("Incorrect Pin");
                            pinErrMsg.setVisibility(View.VISIBLE);
                            pin.requestFocus();
                            //change end icon
                            Animatable loadingIcon = (Animatable) pinInputLayout.getEndIconDrawable();
                            loadingIcon.stop();
                            pinInputLayout.setEndIconDrawable(R.drawable.ic_wrong);

                            if(!TextUtils.isEmpty(district.getText())){
                                Objects.requireNonNull(district.getText()).clear();
                            }
                            if(!TextUtils.isEmpty(state.getText())){
                                Objects.requireNonNull(state.getText()).clear();
                            }
                        }
                    }else {
                        //Toast.makeText(getApplicationContext(),"Can't fetch pin code details!",Toast.LENGTH_LONG).show();
                        pinErrMsg.setText("Can't fetch pin code details!");
                        pinErrMsg.setVisibility(View.VISIBLE);
                        pin.clearFocus();
                    }
                }

                @Override
                public void onFailure(Call<List<PinCodeInfo>> call, Throwable t) {

                    Toast.makeText(getApplicationContext(),"Something went wrong!!",Toast.LENGTH_LONG).show();
                    Log.d("Retrofit pin code error",t.getMessage());

                }
            });
        }
    };

    boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    //create circular animated drawable
    Drawable getDrawable(){
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.progressBarStyleSmall,value,false);
        int[] attrs = {android.R.attr.indeterminateDrawable};
        TypedArray array = obtainStyledAttributes(value.data,attrs);
        Drawable drawable = array.getDrawable(0);
        array.recycle();

        return drawable;
    }
}