package com.example.testingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testingapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    TextView cityName;
    Button search;
    TextView show;
    String url;
    Button toggleLanguage;
    ImageView backgroundImage;

    SharedPreferences sharedPreferences;
    boolean isHindi; // Check the current language

    class getWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                // Check if the request was successful
                int responseCode = urlConnection.getResponseCode();
                if (responseCode != 200) {
                    // If the response code is not 200, return a specific error message
                    return "error";
                }

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "error"; // Return "error" if there is an exception
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Check if the result is "error", meaning the city was not found or an issue occurred
            if (result.equals("error")) {
                show.setText("cannot_fetch_weather"); // Display error message
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");

                // Extract temperature information
                double tempCelsius = main.getDouble("temp") - 273.15;
                double feelsLikeCelsius = main.getDouble("feels_like") - 273.15;
                double tempMaxCelsius = main.getDouble("temp_max") - 273.15;
                double tempMinCelsius = main.getDouble("temp_min") - 273.15;

                // Extract weather condition
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String weatherCondition = weatherObject.getString("main");

                // Extract wind information
                JSONObject wind = jsonObject.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");
                int windDirection = wind.getInt("deg");

                // Convert wind speed from m/s to km/h
                double windSpeedKmH = windSpeed * 3.6;

                // Display weather information including wind in km/h
                String weatherInfo = getString(R.string.temperature) + " : " + String.format("%.2f", tempCelsius) + " °C\n" +
                        getString(R.string.feels_like) + " : " + String.format("%.2f", feelsLikeCelsius) + " °C\n" +
                        getString(R.string.temp_max) + " : " + String.format("%.2f", tempMaxCelsius) + " °C\n" +
                        getString(R.string.temp_min) + " : " + String.format("%.2f", tempMinCelsius) + " °C\n" +
                        getString(R.string.pressure) + " : " + main.getString("pressure") + " hPa\n" +
                        getString(R.string.humidity) + " : " + main.getString("humidity") + "%\n" +
                        getString(R.string.wind_speed) + " : " + String.format("%.2f", windSpeedKmH) + " km/h\n" +
                        getString(R.string.wind_direction) + " : " + windDirection + "°";

                show.setText(weatherInfo);

                // Update background based on weather condition
                updateBackground(weatherCondition);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences to store language preference
        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        isHindi = sharedPreferences.getBoolean("isHindi", false);

        // Set app locale based on saved language preference
        setAppLocale(isHindi ? "hi" : "en", false);  // Skip recreation on first launch

        setContentView(R.layout.activity_main);
        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);
        toggleLanguage = findViewById(R.id.toggleLanguage);
        backgroundImage = findViewById(R.id.imageView);

        final String[] temp = {""};

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, getString(R.string.searching), Toast.LENGTH_SHORT).show();
                String city = cityName.getText().toString();
                try {
                    if (!city.isEmpty()) {
                        url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=ffe4013561b7de8e960aa7636c5236c4";
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.enter_city), Toast.LENGTH_SHORT).show();
                    }
                    getWeather task = new getWeather();
                    temp[0] = task.execute(url).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (temp[0] == null) {
                    show.setText(getString(R.string.weather_info));
                }
            }
        });

        // Update toggle language button text
        toggleLanguage.setText(isHindi ? getString(R.string.switch_to_english) : getString(R.string.switch_to_hindi));

        // Toggle language button
        toggleLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHindi = !isHindi;
                setAppLocale(isHindi ? "hi" : "en", true);
                toggleLanguage.setText(isHindi ? getString(R.string.switch_to_english) : getString(R.string.switch_to_hindi));

                // Save the new language preference
                saveLanguagePreference(isHindi);
            }
        });
    }

    // Save the language preference in SharedPreferences
    private void saveLanguagePreference(boolean isHindi) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isHindi", isHindi);
        editor.apply();
    }

    // Method to switch language and recreate activity (recreate only when needed)
    private void setAppLocale(String languageCode, boolean shouldRecreate) {
        Locale currentLocale = getResources().getConfiguration().locale;
        Locale newLocale = new Locale(languageCode);

        // Only set the new locale and recreate if the new locale is different from the current one
        if (!currentLocale.getLanguage().equals(newLocale.getLanguage())) {
            Locale.setDefault(newLocale);
            Resources resources = getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(newLocale);
            resources.updateConfiguration(configuration, displayMetrics);

            if (shouldRecreate) {
                recreate();
            }
        }
    }

    private void updateBackground(String weatherCondition) {
        if (weatherCondition.equalsIgnoreCase("Clear")) {
            backgroundImage.setImageResource(R.drawable.clear_sky);
        } else if (weatherCondition.equalsIgnoreCase("Clouds")) {
            backgroundImage.setImageResource(R.drawable.cloudy);
        } else if (weatherCondition.equalsIgnoreCase("Rain")) {
            backgroundImage.setImageResource(R.drawable.rainy);
        } else if (weatherCondition.equalsIgnoreCase("Snow")) {
            backgroundImage.setImageResource(R.drawable.snow);
        } else {
            backgroundImage.setImageResource(R.drawable.default_weather);
        }
    }
}
