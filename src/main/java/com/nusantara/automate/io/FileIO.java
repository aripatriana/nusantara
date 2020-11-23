package com.nusantara.automate.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.reader.BufferedFileReader;
import com.nusantara.automate.util.StringUtils;

public class FileIO {

	private static final Logger log = LoggerFactory.getLogger(FileIO.class);
	
	public static Map<String, Object> loadMapValueFile(File path, String separator) {
		BufferedFileReader reader = new BufferedFileReader(path);
		Map<String, Object> data = new HashMap<String, Object>();
		while(reader.iterate()) {
			String text = reader.read();
			String last = StringUtils.removeCharIndex(text, separator, 0);
			
			data.put(text.replace(separator+last, ""), last);
		}
		return data;		
	}
	public static Map<String, Object> loadMapValueFile(String path, String separator) {
		return loadMapValueFile(new File(path), separator);
	}
	
	public static void main(String[] args) {
		FileIO.loadMapValueFile("D:\\error.txt", "=");
	}
	
	public static Properties loadProperties(String path) {
		File file = new File(path); 
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			log.error("ERROR ", e);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
		return prop;
	}
	
	public static void write(String path, Map<String, Object> data) {
		FileWriter writer;
		try {
			writer = new FileWriter(path);
			for (Entry<String, Object> e : data.entrySet()) {
				writer.write(e.getKey() + "=" + e.getValue().toString());			
			}
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
