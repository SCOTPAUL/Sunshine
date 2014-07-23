package uk.co.paulcowie.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ArrayAdapter<String> myArrayAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        //Handle options in menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            String[] parsedData;
            weatherTask.execute("PA4");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sunshine, container, false);
        String fakeData[] = {"Today - Sunny - 25", "Tomorrow - Sunny - 25",
                "Thurs - Rainy - 28", "Fri - Sunny - 15", "Sat - Rainy - 10", "Sun - Rainy - 5",
                "Mon - Rainy - 10", "Tues - Meteor Storm - 15", "Wed - Volcano Eruption - 2500",
                "Thurs - End of Days - NaN"};

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(fakeData));

        myArrayAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_text_view, weekForecast);

        final ListView myListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        myListView.setAdapter(myArrayAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String weatherText = myArrayAdapter.getItem(position);
                Intent detailIntent = new Intent().setClass(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, weatherText);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        //GOOGLE's CODE

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
            if (params.length == 0) {
                //Fail if no postcode specified
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
                StringBuffer buffer = new StringBuffer();
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                WeatherDataParser JSONParser = new WeatherDataParser();
                try {
                    //Return the parsed data, accessible by onPostExecute.
                    returnData = JSONParser.getWeatherDataFromJSONString(forecastJsonStr, 14);
                    return returnData;

                } catch (JSONException e) {
                    //If json parsing fails, can't return string.
                    return null;
                }
            } catch (
                    IOException e
                    )

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
        protected void onPostExecute(String[] result) {
            if (result != null) {

                myArrayAdapter.clear();
                myArrayAdapter.addAll(result);

            }


        }
    }
}


