package com.example.weathernow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temaperatureTV,conditionsTV;
    private ImageView backIV,iconIV,searchIV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temaperatureTV = findViewById(R.id.idTVTemperature);
        conditionsTV= findViewById(R.id.idTVConditions);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEDTCity);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null){cityName = getCityName(location.getLongitude(),location.getLatitude());
            getWeatherInfo(cityName);
        } else {
            cityName = "Delhi";
            getWeatherInfo(cityName);
        }

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "City Name cannot be Empty", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(city);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Permission Granted...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Please grant the permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "not found!";
        Geocoder GCD = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = GCD.getFromLocation(longitude,latitude,10);
            for (Address adr: addresses) {
                if(adr!=null){
                    String city = adr.getLocality();
                    if(city!=null && city.equals("")){
                        cityName = city;
                    }else{
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this, "User city not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
      String url = "http://api.weatherapi.com/v1/forecast.json?key=069b889af95140ce8c960840221505&q="+cityName+"&days=1&aqi=yes&alerts=yes";
      cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
                try {
                  String temperature = response.getJSONObject("current").getString("temp_c");
                  temaperatureTV.setText(temperature+"°C");
                  int isDay = response.getJSONObject("current").getInt("is_day");
                  String conditions = response.getJSONObject("current").getJSONObject("condition").getString("text");
                  String conditionsIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                  Picasso.get().load("http:".concat(conditionsIcon)).into(iconIV);
                  conditionsTV.setText(conditions);
                  if(isDay==1){
                      Picasso.get().load("https://prnt.sc/tDO4wkvq6m-x").into(backIV);
                  }else{
                      Picasso.get().load("https://prnt.sc/IDZX1xIkzsi1").into(backIV);
                    }
                  JSONObject forecast = response.getJSONObject("forecast");
                  JSONObject forecast0 = forecast.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecast0.getJSONArray("hour");
                    for(int i =0;i<hourArray.length();i++){
                        JSONObject hourOBJ = hourArray.getJSONObject(i);
                        String time = hourOBJ.getString("time");
                        String temp = hourOBJ.getString("temp_c");
                        String condIcon = hourOBJ.getJSONObject("condition").getString("icon");
                        String wind = hourOBJ.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(time,temp,condIcon,wind));
                   }
                    weatherRVAdapter.notifyDataSetChanged();
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter valid city name", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}