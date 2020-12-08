package com.nusantara.automate;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.exception.XlsSheetStyleException;
import com.nusantara.automate.reader.MultiLayerXlsFileReader;
import com.nusantara.automate.util.MapUtils;


/**
 * This used for process data file
 * 
 * @author ari.patriana
 *
 */
public class FileRetention implements Retention {

	Logger log = LoggerFactory.getLogger(FileRetention.class);
	
	protected FileReader<Map<String, Object>> fileReader;
	
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
	protected void setHeaderInfo(Map<String, Object> header, Map<String, Object> data) {
		Map<String, Object> copied = new LinkedHashMap<String, Object>(data);
		data.clear();
		for (Entry<String, Object> entry : copied.entrySet()) {
			String key[] = entry.getKey().split("\\.");
			
			String entryKey = key[0];
			if (key.length > 1) entryKey = key[1];

			Object title = header.get(entryKey);
			if (title != null) {
				if (title instanceof Map) {
					Map<String, Object> h = (Map<String, Object>) title;
					Collection<Map<String, Object>> d = (Collection<Map<String, Object>>) entry.getValue();
					for (Map<String, Object> v : d) {
						setHeaderInfo(h, v);
					}
	
					data.put(entry.getKey().replace(" ", "_"), d);
				} else {
					if (key.length > 1) {
						title = key[0].concat(".".concat(title.toString()));
						
					}
					data.put(title.toString().replace(" ", "_"), entry.getValue());	
				}
			} else {
				data.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public void perform(WebExchange webExchange) {
		while(fileReader.iterate()) {
			Map<String, Object> metadata = fileReader.read();
			if (!fileReader.skipHeader())
				setHeaderInfo(fileReader.getHeader(), metadata);
			MapUtils.keyLowercase(metadata);
			webExchange.addMetadata(metadata);
		}
		
		fileReader.close();
	}
	
	@Override
	public int getSize() {
		return fileReader.getSize();
	}
	
	public static void main(String[] args) throws XlsSheetStyleException {
		new FileRetention(new MultiLayerXlsFileReader(new File("D:\\tscen-001.xlsx"))).perform(new WebExchange());;
	}

}
