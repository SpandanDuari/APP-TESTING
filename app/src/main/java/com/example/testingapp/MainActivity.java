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
import android.widget.TextView;
import android.widget.Toast;
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
    boolean isHindi; // To track current language

    private static final String PREFS_NAME = "language_prefs";
    private static final String LANGUAGE_STATE = "language_state";

    private SharedPreferences prefs;

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

                String weatherInfo = getString(R.string.temperature) + " : " + String.format("%.2f", tempCelsius) + " °C\n" +
                        getString(R.string.feels_like) + " : " + String.format("%.2f", feelsLikeCelsius) + " °C\n" +
                        getString(R.string.temp_max) + " : " + String.format("%.2f", tempMaxCelsius) + " °C\n" +
                        getString(R.string.temp_min) + " : " + String.format("%.2f", tempMinCelsius) + " °C\n" +
                        getString(R.string.pressure) + " : " + main.getString("pressure") + " hPa\n" +
                        getString(R.string.humidity) + " : " + main.getString("humidity") + "%";

                show.setText(weatherInfo);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isHindi = prefs.getBoolean(LANGUAGE_STATE, false);

        setAppLocale();

        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);
        toggleLanguage = findViewById(R.id.toggleLanguage);

        // Set initial button text based on language state
        updateToggleButtonText();

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
            @ Override
            public void onClick(View v) {
                // Toggle the language state
                if (isHindi) {
                    setAppLocale("en");  // Use "en" for English } else {
                    setAppLocale("hi");  // Use "hi" for Hindi
                }
                isHindi = !isHindi; // Flip the language state
                prefs.edit().putBoolean(LANGUAGE_STATE, isHindi).apply(); // Save the new language state
                updateToggleButtonText(); // Update button text based on new state
            }
        });
    }

    private void setAppLocale() {
        if (isHindi) {
            setAppLocale("hi");  // Use "hi" for Hindi
        } else {
            setAppLocale("en");  // Use "en" for English
        }
    }

    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, displayMetrics);
    }

    private void updateToggleButtonText() {
        // Update the toggle button text based on the current language state

            toggleLanguage.setText(getString(R.string.switch_to_hindi));

    }
}