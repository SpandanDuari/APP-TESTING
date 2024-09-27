package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.content.res.Resources;
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
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    TextView cityName;
    Button search;
    TextView show;
    String url;


    class getWeather extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls){
            StringBuilder result = new StringBuilder();
            try{
                URL url= new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line="";
                while((line = reader.readLine()) != null){
                    result.append(line).append("\n");
                }
                return result.toString();
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString("main");

                weatherInfo = weatherInfo.replace("temp", getString(R.string.temperature));
                weatherInfo = weatherInfo.replace("feels_like", getString(R.string.feels_like));
                weatherInfo = weatherInfo.replace("temp_max", getString(R.string.temp_max));
                weatherInfo = weatherInfo.replace("temp_min", getString(R.string.temp_min));
                weatherInfo = weatherInfo.replace("pressure", getString(R.string.pressure));
                weatherInfo = weatherInfo.replace("humidity", getString(R.string.humidity));

                weatherInfo = weatherInfo.replace("{", "");
                weatherInfo = weatherInfo.replace("}", "");
                weatherInfo = weatherInfo.replace(",", "\n");
                weatherInfo = weatherInfo.replace(":", " : ");

                show.setText(weatherInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Button toggleLanguage;
        boolean isEnglish = true;

        toggleLanguage = findViewById(R.id.toggleLanguage);

toggleLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnglish) {
                    setLocale("hi");
                } else {
                    setLocale("en");
                }
                isEnglish = !isEnglish;
            }
        });

        private void setLocale(String langCode) {
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = ((Resources) resources).getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());

            // Refresh the activity to apply the new language
            recreate();
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
                Toast.makeText(MainActivity.this, "Button Clicked! ", Toast.LENGTH_SHORT).show();
                String city = cityName.getText().toString();
                try{
                    if(city!=null){
                        url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid={Enter your api Key Here}";
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
