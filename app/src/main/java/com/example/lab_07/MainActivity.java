package com.example.lab_07;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize request queue
        requestQueue = Volley.newRequestQueue(this);

        // Define UI components
        EditText cityInputField = findViewById(R.id.editTextCityName);
        Button weatherButton = findViewById(R.id.buttonGetWeather);
        TextView temperatureView = findViewById(R.id.textViewTemperature);
        TextView cityView = findViewById(R.id.textViewCityName);  // Fixed: Updated to a proper TextView
        ImageView iconView = findViewById(R.id.weatherIcon);
        TextView minTemperatureView = findViewById(R.id.minTemp);
        TextView maxTemperatureView = findViewById(R.id.maxTemp);
        TextView humidityView = findViewById(R.id.textViewHumidity);
        TextView weatherDescriptionView = findViewById(R.id.description);

        // Handle button click to fetch weather data
        weatherButton.setOnClickListener(view -> {
            String cityName = cityInputField.getText().toString();
            if (cityName.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                return;
            }

            String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
            String apiKey = getString(R.string.weather_api_key);  // Store API key securely in strings.xml or BuildConfig
            String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                    + encodedCityName + "&appid=" + apiKey + "&units=metric";
            fetchWeatherData(weatherApiUrl, temperatureView, cityView, minTemperatureView,
                    maxTemperatureView, humidityView, weatherDescriptionView, iconView);
        });
    }

    private void fetchWeatherData(String url, TextView tempTextView, TextView cityTextView,
                                  TextView minTempTextView, TextView maxTempTextView,
                                  TextView humidityTextView, TextView descriptionTextView,
                                  ImageView weatherIconView) {

        @SuppressLint("SetTextI18n") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Extract weather details
                        JSONObject mainData = response.getJSONObject("main");
                        double currentTemperature = mainData.getDouble("temp");
                        double minTemperature = mainData.getDouble("temp_min");
                        double maxTemperature = mainData.getDouble("temp_max");
                        int humidityLevel = mainData.getInt("humidity");
                        String weatherDescription = response.getJSONArray("weather").getJSONObject(0).getString("description");

                        String weatherIconCode = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("icon");

                        String city = response.getString("name");

                        // Update the UI with the fetched data
                        runOnUiThread(() -> {
                            tempTextView.setText("Temperature: " + currentTemperature + "°C");
                            tempTextView.setVisibility(TextView.VISIBLE);

                            cityTextView.setText("City: " + city);
                            cityTextView.setVisibility(TextView.VISIBLE);

                            maxTempTextView.setText("Max Temp: " + maxTemperature + "°C");
                            maxTempTextView.setVisibility(TextView.VISIBLE);

                            minTempTextView.setText("Min Temp: " + minTemperature + "°C");
                            minTempTextView.setVisibility(TextView.VISIBLE);

                            humidityTextView.setText("Humidity: " + humidityLevel + "%");
                            humidityTextView.setVisibility(TextView.VISIBLE);

                            descriptionTextView.setText("Description: " + weatherDescription);
                            descriptionTextView.setVisibility(TextView.VISIBLE);

                            String iconUrl = "https://openweathermap.org/img/w/" + weatherIconCode + ".png";
                            loadWeatherIcon(weatherIconView, iconUrl);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch data!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void loadWeatherIcon(ImageView iconImageView, String iconUrl) {
        ImageRequest imageRequest = new ImageRequest(iconUrl,
                bitmap -> {
                    iconImageView.setImageBitmap(bitmap);
                    iconImageView.setVisibility(ImageView.VISIBLE);
                },
                0, 0, ImageView.ScaleType.CENTER, null,
                error -> Toast.makeText(this, "Failed to load icon!", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(imageRequest);
    }
}
