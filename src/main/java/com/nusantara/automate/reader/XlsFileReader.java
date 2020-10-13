package com.nusantara.automate.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nusantara.automate.FileReader;

public class XlsFileReader implements FileReader<Map<String, Object>> {

	private File file;
	private int activeSheet;
	private Workbook workbook;
	private Map<Integer, LinkedList<Map<String, Object>>> container;
	private LinkedHashMap<String, Object> header;
	private LinkedList<Map<String, Object>> data;
	private LinkedList<Map<String, Object>> dataCompile;
	
	public XlsFileReader(File file) {
		this.file = file;
		try {
			header = new LinkedHashMap<String, Object>();
			workbook = new XSSFWorkbook(new FileInputStream(file));
			activeSheet = workbook.getNumberOfSheets();
			container = new HashMap<Integer, LinkedList<Map<String, Object>>>();
			for (int index = 0; index<activeSheet; index++) {
				Sheet sheet = workbook.getSheetAt(index);
				if (!sheet.getSheetName().equalsIgnoreCase("meta-data")) {
					XlsSheetReader<LinkedHashMap<String, Object>> dataSheet = new XlsSheetReader<LinkedHashMap<String, Object>>(new XlsCustomRowReader(workbook.getSheetAt(index)));
					LinkedHashMap<Integer, LinkedHashMap<String, Object>> dataPerSheet = dataSheet.readSheet(skipHeader());
					
					List<Object> removed = normalize(dataPerSheet);
					
					if (!skipHeader()) {
						header = dataPerSheet.remove(0);
						normalizeHeader(removed, header);
					}
					
					container.put(index, new LinkedList<Map<String, Object>>(dataPerSheet.values()));
				}
			}
			
			data = new LinkedList<Map<String, Object>>();
			for (LinkedList<Map<String, Object>> d : container.values()) {
				data.addAll(d);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void normalizeHeader(List<Object> removed, LinkedHashMap<String, Object> dataHeader) {
		if (removed != null && removed.size() > 0) {
			LinkedHashMap<String, Object> removedHeader = new LinkedHashMap<String, Object>(); 
			int i = 0;
			for (Object rem : removed) {
				removedHeader.put(i+"", dataHeader.remove(rem));
				i++;
			}
			
			dataHeader.put("SECURITY_LIST", removedHeader);
		}
	}
	
	/**
	 * before
	 * TLKM|ANTM
	 * 1000|1500
	 * 
	 * after
	 * [TLKM, 1000][ANTM,1500]
	 * 
	 * @param dataPerSheet
	 */
	public List<Object> normalize(LinkedHashMap<Integer, LinkedHashMap<String, Object>> dataPerSheet) {
		List<Object> removed = null;
		for (Map<String, Object> data : dataPerSheet.values()) {

			int arraySize = 0;
			removed = new ArrayList<Object>();
			
			// detect pipe
			LinkedList<String[]> arrayList = new LinkedList<String[]>();
			for (Entry<String, Object> entry : data.entrySet()) {
				if (entry.getValue() != null) {
					String value = entry.getValue().toString();
					
					if (value.contains("|")) {
						removed.add(entry.getKey());
						
						String[] values = value.split("\\|");
						arrayList.add(values);
						arraySize = values.length;
					}					
				}
			}
			
			// normalisasi
			LinkedHashMap<Integer, LinkedHashMap<String, Object>> normalize = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();
			for (int i=0; i<arraySize; i++) {
				normalize.put(i, new LinkedHashMap<String, Object>());
			}
			
			int z = 0;
			for (String[] arr : arrayList) {
				for (int i=0; i< arraySize; i++) {
					Map<String, Object> d = normalize.get(i);
					d.put(z+"", arr[i]);
				}
				z++;
			}
			
			// replace
			for (Object rem : removed) {
				data.remove(rem);
			}
			
			if (normalize.size() > 0) {
				data.put("SECURITY_LIST", normalize.values());
			}
		}
		return removed;
	}
	
	@Override
	public File getFile() {
		return file;
	}

	@Override
	public boolean skipHeader() {
		return false;
	}

	@Override
	public Map<String, Object> getHeader() {
		return header;
	}

	@Override
	public boolean iterate() {
		if (dataCompile == null)
			dataCompile = new LinkedList<Map<String,Object>>(data);
		return !dataCompile.isEmpty();
	}

	@Override
	public Map<String, Object> read() {
		return dataCompile.removeFirst();
	}
	
	@Override
	public void close() {
		dataCompile.clear();
		data.clear();
		header.clear();
		container.clear();
	}
}
