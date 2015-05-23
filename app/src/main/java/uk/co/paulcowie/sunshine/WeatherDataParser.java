package uk.co.paulcowie.sunshine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by paul on 23/07/14.
 */

public class WeatherDataParser {


    private String getHumanReadableDate(long time) {
        //Formats UNIX timestamp to human readable date
        //*1000 to get ms
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date);

    }

    private String formatTemperatures(double high, double low) {
        //Rounds off the exact high/low temperatures produced by JSON formatter
        long roundHigh = Math.round(high);
        long roundLow = Math.round(low);

        return roundHigh + "/" + roundLow;
    }

    private String formatImperial(double high, double low) {
        double highImp = ((high * 9) / 5) + 32;
        double lowImp = ((low * 9) / 5) + 32;
        return formatTemperatures(highImp, lowImp);
    }

    public String[] getWeatherDataFromJSONString(String JSONString, String unitType)
            throws JSONException {
        //Required fields
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJSON = new JSONObject(JSONString);
        JSONArray weatherArray = forecastJSON.getJSONArray(OWM_LIST);

        ArrayList<String> weatherStrings = new ArrayList<String>();

        for (int i = 0; i < weatherArray.length(); i++) {
            String day;
            String hiLow;
            String description;

            //Get JSON day object
            JSONObject dayStruct = weatherArray.getJSONObject(i);

            //Get date for day
            //Produces UNIX timestamp
            long dateTime = dayStruct.getLong(OWM_DATETIME);
            day = getHumanReadableDate(dateTime);


            //Get weather description
            //Description is in array at index 0
            description = dayStruct.getJSONArray(OWM_WEATHER).getJSONObject(0).getString(OWM_DESCRIPTION);


            //Get temperatures
            JSONObject temperatureObj = dayStruct.getJSONObject(OWM_TEMPERATURE);
            double highTemp = temperatureObj.getDouble(OWM_MAX);
            double lowTemp = temperatureObj.getDouble(OWM_MIN);

            if (unitType.equals("metric")) {
                hiLow = formatTemperatures(highTemp, lowTemp);
            } else if (unitType.equals("imperial")) {
                hiLow = formatImperial(highTemp, lowTemp);
            } else {
                hiLow = "0/0";
            }


            weatherStrings.add(day + " - " + description + " - " + hiLow);


        }
        return weatherStrings.toArray(new String[weatherStrings.size()]);
    }

}

