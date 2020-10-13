package com.nusantara.automate;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileRetention implements Retention {

	Logger log = LoggerFactory.getLogger(FileRetention.class);
	
	private FileReader<Map<String, Object>> fileReader;
	
	public FileRetention(FileReader<Map<String, Object>> fileReader) {
		this.fileReader = fileReader;
	}
	
	public FileReader<Map<String, Object>> getFileReader() {
		return fileReader;
	}
	
	public File getFile() {
		return fileReader.getFile();
	}
	
	@SuppressWarnings("unchecked")
	private void setHeaderInfo(Map<String, Object> header, Map<String, Object> data) {
		Map<String, Object> copied = new LinkedHashMap<String, Object>(data);
		data.clear();
		for (Entry<String, Object> entry : copied.entrySet()) {
			Object title = header.get(entry.getKey());
			if (title instanceof Map) {
				Map<String, Object> h = (Map<String, Object>) title;
				Collection<Map<String, Object>> d = (Collection<Map<String, Object>>) entry.getValue();
				for (Map<String, Object> v : d) {
					setHeaderInfo(h, v);
				}
				data.put(entry.getKey().toUpperCase(), d);
			} else {
				data.put(title.toString().toUpperCase().replace(" ", "_"), entry.getValue());	
			}
				
		}
	}
	
	@Override
	public void perform(WebExchange webExchange) {
		while(fileReader.iterate()) {
			Map<String, Object> metadata = fileReader.read();
			if (!fileReader.skipHeader())
				setHeaderInfo(fileReader.getHeader(), metadata);
			webExchange.addMetadata(metadata);

			log.info("Read metadata : " + metadata);
		}
		fileReader.close();
	}

}
