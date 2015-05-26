/*
 * MyJsonParser.java
 *
 * Version:
 *        1.0
 *
 * Revision:
 *        1.0
 */

/*
 * Wrapper over Json parser that returns hashmap 
 * of values   
 *    
 * @author    Gaurav Joshi
 * @author    Ravi Kumar Singh
 * 
 */



import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


public class MyJsonParser {

	Map<String,Object> parse( String jsonString){
		
		Map<String,Object> map = new HashMap<String,Object>(); 
		JSONObject jsonObj;
		try {
			
			jsonObj = new JSONObject(jsonString);
			String[] keysArray = JSONObject.getNames(jsonObj);
			
			for (int i = 0 ; i < keysArray.length ; ++i){
				
				Object jsonValue = jsonObj.get(keysArray[i]);
				
				if (jsonValue == null){
					
					continue;
				}
				
				map.put(keysArray[i], jsonValue);				
			}

		} catch (JSONException e) {

			e.printStackTrace();
		}
		
		return map;
	}
}
