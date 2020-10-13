package com.nusantara.automate.util;

import java.io.IOException;






import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class to convert Json-Object vice versa
 * @author Fadhil Paramanindo
 *
 */
@Component
public class JsonUtils {

	ObjectMapper objectMapper;
	Logger log=LoggerFactory.getLogger(JsonUtils.class);
	public JsonUtils(){
		objectMapper=new ObjectMapper();
	}

	/**
	 * Convert 	an 						POJO obj into JSON formatted String
	 * @param 	obj 					Source obj
	 * @return 	String 					Convert result from POJO obj
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	public String toJson(Object obj) throws  IOException, JsonMappingException, JsonGenerationException{
		return  objectMapper.writeValueAsString(obj);
	}

	/**
	 * Convert JSON formatted String Into Java POJO obj
	 * @param 	json 				Source string 
	 * @param 	cls 				Class type of returned obj
	 * @return 	Object 				Convert result from JSON formatted String to POJO obj
	 * @throws 	IOException
	 * @throws 	JsonParseException 
	 * 
	 */
	public <T> Object fromJson(String json,Class<T> cls) throws  IOException, JsonParseException{
		return objectMapper.readValue(json, cls);
	}
}
