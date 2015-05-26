/*
 * Predict.java
 *
 * Version:
 *        1.0
 *
 * Revision:
 *        1.0
 */

/*
 * Class that takes input from User   
 * and predicts whether the city is suitable for his living
 * assuming the fact that user is a Student 
 *    
 * @author    Gaurav Joshi
 * @author    Ravi Kumar Singh
 * 
 */


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;


public class Predict {

	public static String keysToRead[] = {"health_care_index","crime_index","hotel_price_index","purchasing_power_incl_rent_index","cpi_index","pollution_index","traffic_index","groceries_index","safety_index","rent_index","restaurant_price_index","property_price_to_income_ratio","Weather"};
	Classifier classifier;
	FastVector attributeList;

	/**
	 * Method to initialize model 
	 * It loads it and creates list of all attributes
	 */
	public void initialize () {

		try {

			classifier= (Classifier) SerializationHelper.read(new FileInputStream("model_weka.model"));

			attributeList = new FastVector(keysToRead.length+1);

			for (int i = 0 ; i < keysToRead.length-1 ; ++i) {

				Attribute attr = new Attribute(keysToRead[i]);
				attributeList.addElement(attr);
			}

			// create fast vector of weather values
			FastVector weatherValues = new FastVector();

			weatherValues.addElement("pleasant");
			weatherValues.addElement("hot");
			weatherValues.addElement("cold");

			Attribute attr13 = new Attribute("Weather", weatherValues);

			// create class attribute
			FastVector classValues = new FastVector();

			classValues.addElement("Good");
			classValues.addElement("Bad");
			Attribute attr14 = new Attribute("Class", classValues);

			attributeList.addElement(attr13);                     
			attributeList.addElement(attr14);

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * 
	 * Takes input city and state from user and outputs prediction
	 * 
	 * @param cityToBeSearched		City 
	 * @param state					State
	 */
	public  void predictForCities(String cityToBeSearched, String state) {

		System.out.println("Calculating..");

		try {
			// do url encoding
			// fetch data from server
			URL apiUrl = new URL ("http://www.numbeo.com/api/indices?api_key=x1gripxovhon7f&query="+URLEncoder.encode(cityToBeSearched, "UTF-8"));			
			HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();		
			Instances data = new Instances( "TestInstances",attributeList, 0);            
			Instance instanceToClassify = new SparseInstance(4);
			data.setClassIndex(13);

			// if response is OK then only go ahead
			if ( HttpURLConnection.HTTP_OK == conn.getResponseCode()){

				BufferedReader reader = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));			 
				String output;
				StringBuffer jsonBuffer = new StringBuffer();

				while ((output = reader.readLine()) != null) {

					jsonBuffer.append(output);
				}

				conn.disconnect();					
				String jsonString = jsonBuffer.toString();
				MyJsonParser parser = new MyJsonParser();
				Map<String,Object> map = parser.parse(jsonString);

				// checking if we got data of same city or not
				String city = (String) map.get( "name" );

				// if no data for city return no point in moving ahead
				if (! city.toLowerCase().contains(cityToBeSearched.toLowerCase())){

					System.out.println("No data for city " + cityToBeSearched);
					return;
				}

				
				// feature selection 
				// if most important features missing dont go ahead 
				if (map.get("pollution_index") == null  && map.get("rent_index") == null) {
					
					System.out.println("Can not classify " + cityToBeSearched);
					return;
				}
				
				// fetch all required attribute values
				for ( int i = 0 ; i < keysToRead.length-1; ++i){

					String key = keysToRead[i];
					Object value = map.get(key);

					if ( null == value){

						instanceToClassify.isMissing((int)attributeList.elementAt(i));
					}

					if (value instanceof Double || value instanceof Float){


						double actualValue = (double)value;
						instanceToClassify.setValue( (Attribute)attributeList.elementAt(i) , actualValue );
					}else if ( value instanceof Long){

						long actualValue = (long) value;
						//							System.out.println(key + ": " + actualValue);
						instanceToClassify.setValue( (Attribute)attributeList.elementAt(i) , actualValue );				
					}else if ( value instanceof Integer ){

						int actualValue = (int) value;
						instanceToClassify.setValue( (Attribute)attributeList.elementAt(i) , actualValue );

					}else if ( value instanceof Float ){

						float actualValue = (float) value;
						instanceToClassify.setValue( (Attribute)attributeList.elementAt(i) , actualValue );

					}else if (value instanceof String){

						String actualValue = (String) value;
						instanceToClassify.setValue( (Attribute)attributeList.elementAt(i) , actualValue );
					}else if (value instanceof JSONObject){

						System.out.println("Another json Oject");
					}else if (value instanceof JSONArray){

						System.out.println("Json Array there");
					}
				}	

			}

			// fetch weather data here 
			ReadingAPI weatherData = new ReadingAPI();
			String str = weatherData.getWeatherValue(cityToBeSearched, state);

			if ( str == null ) {

				instanceToClassify.isMissing((Attribute)attributeList.elementAt(12));
			}else {

				Attribute weather =  ((Attribute)attributeList.elementAt(12));
				int indexOfVal = weather.indexOfValue(str);
				instanceToClassify.setValue((Attribute)attributeList.elementAt(12), weather.value(indexOfVal));				
			}


			// for now marking it as unknown 
			instanceToClassify.isMissing((Attribute)attributeList.elementAt(13));
			data.add(instanceToClassify);
			classifier.classifyInstance(data.firstInstance());
			String result = null;
			double output = classifier.classifyInstance( data.firstInstance() );

			if ( output == 1.0) {

				result = "Suitable";
			}else {

				result = "Not Suitable";				
			}

			// print the final results
			System.out.println("Result: " + result);

		}catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * The main method
	 * @param args	ignored
	 */
	public static void main (String args[]) {

		Predict pred = new Predict();
		pred.initialize();
		
		// take input from user 
		Scanner in = new Scanner(System.in);
		System.out.println("Please enter city:");
		String city = in.nextLine();
		System.out.println("Please enter state(two letter code):");
		String state = in.next();
		
		//predict
		pred.predictForCities( city , state );
		in.close();
	}
}
