package com.example.testingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.*;
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
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    TextView cityName;
    Button search;
    TextView show;
    String url;
    Button toggleLanguage;




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

                // Extract temperature-related information and convert from Kelvin to Celsius
                double tempKelvin = main.getDouble("temp");
                double feelsLikeKelvin = main.getDouble("feels_like");
                double tempMaxKelvin = main.getDouble("temp_max");
                double tempMinKelvin = main.getDouble("temp_min");

                // Convert to Celsius
                double tempCelsius = tempKelvin - 273.15;
                double feelsLikeCelsius = feelsLikeKelvin - 273.15;
                double tempMaxCelsius = tempMaxKelvin - 273.15;
                double tempMinCelsius = tempMinKelvin - 273.15;

                // Format the temperature values for display
                String weatherInfo = "Temperature : " + String.format("%.2f", tempCelsius) + " °C\n" +
                        "Feels Like : " + String.format("%.2f", feelsLikeCelsius) + " °C\n" +
                        "Temperature Max : " + String.format("%.2f", tempMaxCelsius) + " °C\n" +
                        "Temperature Min : " + String.format("%.2f", tempMinCelsius) + " °C\n" +
                        "Pressure : " + main.getString("pressure") + " hPa\n" +
                        "Humidity : " + main.getString("humidity") + "%";

                // Display the formatted weather information
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
        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);

        final String[] temp={""};

        search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this, "Searching !! ", Toast.LENGTH_SHORT).show();
                String city = cityName.getText().toString();
                try{
                    if(city!=null){
                        url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=";
                    }else{
                        Toast.makeText(MainActivity.this, "Enter City", Toast.LENGTH_SHORT).show();
                    }
                    getWeather task= new getWeather();
                    temp[0] = task.execute(url).get();
                }catch(ExecutionException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                if(temp[0] == null){
                    show.setText("Cannot able to find Weather");
                }

            }
        });

    }
}
