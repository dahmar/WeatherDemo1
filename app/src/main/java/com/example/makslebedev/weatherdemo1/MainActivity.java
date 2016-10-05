package com.example.makslebedev.weatherdemo1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    ArrayList<Forecast> forecasts = new ArrayList<>();
    TextView text;

    String format = "http://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=c3193cff99f7c26b8b53226aec4c2587&mode=json";
    WebView browser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //text = (TextView) findViewById(R.id.text);
        browser = (WebView) findViewById(R.id.browser);


    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        UpdateTask task = new UpdateTask();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location==null){
                Toast.makeText(this, "LOCATION == NULL", Toast.LENGTH_LONG).show();
                Log.d("LOCATION", "NULL");
            }else{
                //Log.d("LOCATION==NULL?", Boolean.toString(location==null));
                task.execute(location);
                Log.d("PERMISSION", "GRANTED");
            }
        }else{
            Log.d("PERMISSION", "DENIED");
        }

    }

    private String updateForecast(Location loc){
        Log.d("LOC == NULL?", Boolean.toString(loc==null));
        String urlad = String.format(format, loc.getLatitude(),loc.getLongitude());
        Log.d("URL ADDRESS", urlad);
        String raw = "";
        StringBuilder myStrBuff = new StringBuilder();

        try {
            URL url = new URL(urlad);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "");
           // connection.addRequestProperty("lat", String.valueOf(loc.getLatitude()).substring(0,4));
           // connection.addRequestProperty("lon", String.valueOf(loc.getLongitude()).substring(0,4));
           // connection.addRequestProperty("APPID", "c3193cff99f7c26b8b53226aec4c2587");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

        InputStream input = connection.getInputStream();
        BufferedReader buf = new BufferedReader(new InputStreamReader(input));

        while ((raw = buf.readLine())!=null){
            myStrBuff.append(raw);
        }
    }catch (IOException ioe){
        ioe.printStackTrace();
    }

    Log.d("RESPONCE", myStrBuff.toString());

        return (myStrBuff.toString());
    }

    String buildForecasts(String json){

        JSONObject object;
        ArrayList<Forecast> forecasts = null;
        String html="";

        try {
            forecasts = new ArrayList<>();
            object =(JSONObject) new JSONTokener(json).nextValue();
            JSONArray array = object.getJSONArray("list");

            for (int i = 0; i < array.length(); i++) {
                forecasts.add(getForecast(array.getJSONObject(i)));
            }

            html = generatePage(getCity(object.getJSONObject("city")), forecasts);
             Log.d("JSON ARRAY", String.valueOf(array.length()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return (html);
    }

    City getCity(JSONObject object){
        JSONObject coord;
        City city = new City();
        try {
            city.setName(object.getString("name"));
            city.setCountry(object.getString("country"));
            city.setId(object.getString("id"));
            coord = object.getJSONObject("coord");
            city.setLon(coord.getString("lon"));
            city.setLat(coord.getString("lat"));
            Log.d("CITY LENGTH" , String.valueOf(object.length()) +
                                  "\n CITY INSTANCE " + city.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return (city);
    }

    Forecast getForecast(JSONObject object){
        Forecast forecast = new Forecast();
        JSONObject main, wind, weather;
        JSONArray weatherArray;

        try {
            weatherArray = object.getJSONArray("weather");
            main = object.getJSONObject("main");
            weather = weatherArray.getJSONObject(0);
            wind = object.getJSONObject("wind");

            forecast.setTime(object.getString("dt_txt"));
            forecast.setDescription(weather.getString("description"));
            forecast.setIconUrl(weather.getString("icon"));
            forecast.setTemp(main.getString("temp"));
            forecast.setPressure(main.getString("pressure"));
            forecast.setWindSpeed(wind.getString("speed"));

        }catch(JSONException e){e.printStackTrace();}

        return(forecast);
    }

    String generatePage(City city, ArrayList<Forecast> forecasts){
        StringBuilder bufResult=new StringBuilder("<html><body><h1 align=\"center\">");

        bufResult.append(city.getName());
        bufResult.append(", ");
        bufResult.append(city.getCountry());
        bufResult.append("</h1><h3 align=\"center\">id: ");
        bufResult.append(city.getId());
        bufResult.append("</h3><p align=\"center\">  coordinates: </p><p align=\"center\">lat = ");
        bufResult.append(city.getLat());
        bufResult.append(" </p><p align=\"center\">lon = ");
        bufResult.append(city.getLon());
        bufResult.append("</p><table>");

        bufResult.append("<tr><th>Time</th><th>Temperature</th><th>Pressure</th><th>Weather</th><th>Icon</th>");

        for (Forecast forecast: forecasts){
            bufResult.append("<tr><td align=\"center\"><small>");
            bufResult.append(forecast.getTime());
            bufResult.append("</small></td><td align=\"center\">");
            bufResult.append(forecast.getTemp());
            bufResult.append("</td><td align=\"center\">");
            bufResult.append(forecast.getPressure());
            bufResult.append("</td><td align=\"center\">");
            bufResult.append(forecast.getDescription());
            bufResult.append("</td><td><img src=\"http://openweathermap.org/img/w/");
            bufResult.append(forecast.getIconUrl());
            bufResult.append(".png\"></td></tr>");
        }

        bufResult.append("</table></body></html>");

        return (bufResult.toString());
    }

    class UpdateTask extends AsyncTask<Location,Void,String>{


        @Override
        protected String doInBackground(Location... params) {
            Log.d("PARAMS[0] == NULL?", Boolean.toString(params[0]==null));
            String raw = updateForecast(params[0]);
            String html = buildForecasts(raw);
            return html;
        }

        @Override
        protected void onPostExecute(String s) {
            //text.setText(s);
            browser.loadDataWithBaseURL(null, s, "text/html", "UTF-8", null);
        }
    }
}

class Forecast{
    String time = "";
    String temp = "";
    String iconUrl = "";
    String pressure = "";
    String description = "";
    String windSpeed = "";

    @Override
    public String toString() {
        return "Forecast{" +
                "time='" + time + '\'' +
                ", temp='" + temp + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", pressure='" + pressure + '\'' +
                ", description='" + description + '\'' +
                ", windSpeed='" + windSpeed + '\'' +
                '}';
    }

    public String getTime() {
        return time;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    void setTime(String time){
        this.time = time;
    }

    String getIconUrl(){
        return (iconUrl);
    }

    void setIconUrl(String iconUrl){
        this.iconUrl = iconUrl;
    }

    public String getTemp() {
        return temp;
    }
}

class City {
    String name;
    String lon, lat;
    String country;
    String id;

    @Override
    public String toString() {
        return "City{\n" +
                "name='" + name + '\'' +
                ", \nlon='" + lon + '\'' +
                ", lat='" + lat + '\'' +
                ", \ncountry='" + country + '\'' +
                ", \nid='" + id + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
