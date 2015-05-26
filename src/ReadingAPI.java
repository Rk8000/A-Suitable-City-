import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * The ReadingAPI class read the weather API from wunderground and return the type of weather in the particular cities.
 *
 * @author  Gaurav Pradip Joshi, Ravi Kumar Singh
 * @version 1.2
 * @since   2015-05-15
 */
public class ReadingAPI {

    /**
     * typeOfWeather function return the weather type
     * @param  value
     * @return void
     */
    private String typeOfWeather(String value) {
    	
    	if (value == null || value.equals("")) {
    		
    		return null;
    	}
    	
        int temp = Integer.parseInt(value);
        
        if(temp <=80){

        	return "cold";
        } else if( temp >=81 && temp <90){

            return "pleasant";
        } else if(temp >=90) {
        	
            return "hot";
        }
        
        return null;
    }

    /**
     * readAll function read the line
     * @param  bufferedReader
     * @return String
     * @throws java.io.IOException
     */
    private static String readAll(Reader bufferedReader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int currentLine;
        while ((currentLine = bufferedReader.read()) != -1) {
            stringBuilder.append((char) currentLine);
        }
        return stringBuilder.toString();
    }

    /**
     * readingWeatherAPI read the weather API and return the JSON object
     * @param  URL, stateName, cityName
     * @return JSONObject
     * @throws java.io.IOException
     */
    private static JSONObject readingWeatherAPI(String URL, String stateName, String cityName) throws IOException {

    	String[] cities = cityName.split(" ");
        if( cities.length >1){
            cityName = cities[0]+ "_" + cities[1];
        }
        URL = URL + stateName + "/" + cityName + "." + "json";
        InputStream is = new URL(URL).openStream();
        JSONObject json = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(bufferedReader);
            json = new JSONObject(jsonText);
                    
        } catch (JSONException e) {

        	e.printStackTrace();
		} finally {
			
            is.close();
        }
        return json;
    }

    /**
     * The main method which .
     * @param args not used
     * @return void
     * @throws java.lang.Exception
     */
    public String getWeatherValue(String cityName, String stateName) throws IOException, ParseException {

    	String URL = "http://api.wunderground.com/api/4f83fc1402fd3af8/planner_06010702/q/";
        JSONObject Jsonobject = readingWeatherAPI(URL,stateName, cityName );
        JSONObject trip;
        String temp = null;
        
        if ( Jsonobject == null ) {
        	
        	return null;
        }
        
		try {
			
			trip = (JSONObject) Jsonobject.get("trip");
	        JSONObject temp_high = (JSONObject) trip.get("temp_high");
	        JSONObject temp_high_avg = (JSONObject) temp_high.get("avg");
	        temp = (String) temp_high_avg.get("F");
		} catch (JSONException e) {

			//e.printStackTrace();
			return null;
		}

        return typeOfWeather(temp);
    }

}
