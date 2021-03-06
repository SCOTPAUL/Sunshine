package uk.co.paulcowie.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uk.co.paulcowie.sunshine.util.ForecastListAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ForecastListAdapter myArrayAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        //Handle options in menu
        setHasOptionsMenu(true);
        Log.v(LOG_TAG, "Created");
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public void updateWeather() {
        //Fetch weather from OpenWeatherMap
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.location_setting_key), getString(R.string.location_setting_default));
        String unitType = prefs.getString(getString(R.string.units_setting_key), getString(R.string.units_setting_default));

        //Use asynctask to get data
        weatherTask.execute(location, unitType);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        //If there is an app to get the map request...
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "Started");
        updateWeather();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "Stopped");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "Destroyed");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "Paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "Resumed");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        } else if (id == R.id.action_map) {
            //Send an intent to open a map at postcode set in settings
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            //Default to PA4 if no location set
            String postcode = prefs.getString(getString(R.string.location_setting_key), getString(R.string.location_setting_default));
            String URI_START = "geo:0,0?";

            Uri locationUri = Uri.parse(URI_START).buildUpon()
                    .appendQueryParameter("q", postcode)
                    .build();

            showMap(locationUri);
            return true;


        }
        //Pass all other clicks to the super class
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sunshine, container, false);

        List<String> weekForecast = new ArrayList<String>();

        myArrayAdapter = new ForecastListAdapter(getActivity(), weekForecast);

        final ListView myListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        myListView.setAdapter(myArrayAdapter);

        //On list item click...
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Send an intent to DetailActivity, starting it and passing in the data held by
                //the list item.
                String weatherText = myArrayAdapter.getItem(position);
                Intent detailIntent = new Intent().setClass(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, weatherText);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        //Tag for use in Log statements.
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
        //Uri object
        Uri myUri;
        private String[] returnData;


        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0 || params.length == 1) {
                //Fail if no postcode or temperature unit selected
                Log.v(LOG_TAG, "Parameters not correctly set");
                return null;
            }

            try
            {

                //URI Builder for openweathermap
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                //Get api key from http://openweathermap.org/appid
                final String API_PARAM = "APPID";

                myUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, "json")
                        .appendQueryParameter(UNITS_PARAM, "metric")
                        .appendQueryParameter(DAYS_PARAM, "14")
                                //Get api key from http://openweathermap.org/appid
                                //Hidden class ApiKeyHandler stores api keys
                        .appendQueryParameter(API_PARAM, ApiKeyHandler.OWM_KEY)
                        .build();


                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(myUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    Log.v(LOG_TAG, "Buffer empty");
                    return null;
                }
                forecastJsonStr = buffer.toString();
                WeatherDataParser JSONParser = new WeatherDataParser();

                //String for unit type required after parsing
                String unitType = params[1];
                try {
                    //Return the parsed data, accessible by onPostExecute.
                    returnData = JSONParser.getWeatherDataFromJSONString(forecastJsonStr, unitType);
                    Log.v(LOG_TAG, unitType);
                    return returnData;

                } catch (JSONException e) {
                    //If json parsing fails, can't return string.
                    Log.e(LOG_TAG, "JSON parsing failed", e);
                    return null;
                }
            } catch (IOException e)

            {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no
                // point in attempting to parse it.
                return null;
            } finally

            {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

        }

        @Override
        protected void onPostExecute(String[] results) {
            if (results != null) {
                //Set the array adapter to hold the contents of the returned
                //weather data once it has been fetched
                myArrayAdapter.clear();
                Log.v(LOG_TAG, String.valueOf(results.length) + " items retrieved");

                for (String result : results) {
                    myArrayAdapter.add(result);
                }


            }


        }
    }
}


