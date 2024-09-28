package com.example.testingapp;

import androidx.appcompat.app.AppCompatActivity;
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
    boolean isHindi = false;
    ImageView backgroundImage;  // ImageView for background

    class getWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");

                double tempCelsius = main.getDouble("temp") - 273.15;
                double feelsLikeCelsius = main.getDouble("feels_like") - 273.15;
                double tempMaxCelsius = main.getDouble("temp_max") - 273.15;
                double tempMinCelsius = main.getDouble("temp_min") - 273.15;

                // Get weather condition from JSON
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String weatherCondition = weatherObject.getString("main");

                // Display weather information
                String weatherInfo = getString(R.string.temperature) + " : " + String.format("%.2f", tempCelsius) + " °C\n" +
                        getString(R.string.feels_like) + " : " + String.format("%.2f", feelsLikeCelsius) + " °C\n" +
                        getString(R.string.temp_max) + " : " + String.format("%.2f", tempMaxCelsius) + " °C\n" +
                        getString(R.string.temp_min) + " : " + String.format("%.2f", tempMinCelsius) + " °C\n" +
                        getString(R.string.pressure) + " : " + main.getString("pressure") + " hPa\n" +
                        getString(R.string.humidity) + " : " + main.getString("humidity") + "%";

                show.setText(weatherInfo);

                // Update the background based on weather condition
                updateBackground(weatherCondition);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);
        toggleLanguage = findViewById(R.id.toggleLanguage);
        backgroundImage = findViewById(R.id.imageView); // Get reference to background ImageView

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

        toggleLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHindi) {
                    setAppLocale("en");
                    toggleLanguage.setText(getString(R.string.switch_to_hindi));
                } else {
                    setAppLocale("hi");
                    toggleLanguage.setText(getString(R.string.switch_to_english));
                }
                isHindi = !isHindi;
            }
        });
    }

    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, displayMetrics);

        // Refresh the activity to apply language change
        recreate();
    }

    // Function to update background based on weather condition
    private void updateBackground(String weatherCondition) {
        if (weatherCondition.equalsIgnoreCase("Clear")) {
            backgroundImage.setImageResource(R.drawable.rainy);  // Set a clear sky background
        } else if (weatherCondition.equalsIgnoreCase("Clouds")) {
            backgroundImage.setImageResource(R.drawable.cloudy);  // Set a cloudy background
        } else if (weatherCondition.equalsIgnoreCase("Rain")) {
            backgroundImage.setImageResource(R.drawable.rainy);  // Set a rainy background
        } else if (weatherCondition.equalsIgnoreCase("Snow")) {
            backgroundImage.setImageResource(R.drawable.snow);  // Set a snowy background
        } else {
            backgroundImage.setImageResource(R.drawable.default_weather);  // Set a default background for other weather conditions
        }
    }
}
