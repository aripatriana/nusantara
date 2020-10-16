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

/**
 * Read xls file and process it to the format that known by the system
 * this support for multiple list in a row
 * 
 * @author ari.patriana
 *
 */
public class MadnessXlsFileReader implements FileReader<Map<String, Object>> {

	protected File file;
	protected int activeSheet;
	protected Workbook workbook;
	protected Map<Integer, LinkedList<Map<String, Object>>> container;
	protected LinkedHashMap<String, Object> header;
	protected LinkedList<Map<String, Object>> data;
	protected LinkedList<Map<String, Object>> dataCompile;
	protected Map<String, Object> currentRow;
	
	protected MadnessXlsFileReader() {
	}
	
	public MadnessXlsFileReader(File file) {
		this.file = file;
		try {
			header = new LinkedHashMap<String, Object>();
			workbook = new XSSFWorkbook(new FileInputStream(file));
			activeSheet = workbook.getNumberOfSheets();
			container = new HashMap<Integer, LinkedList<Map<String, Object>>>();
			for (int index = 0; index<activeSheet; index++) {
				Sheet sheet = workbook.getSheetAt(index);
				if (!sheet.getSheetName().equalsIgnoreCase("meta-data")) {
					XlsSheetReader<LinkedHashMap<String, Object>> dataSheet = new XlsSheetReader<LinkedHashMap<String, Object>>(new XlsCustomRowReader(sheet));
					LinkedHashMap<Integer, LinkedHashMap<String, Object>> dataPerSheet = dataSheet.readSheet(skipHeader());
					
					
					Map<String, LinkedList<Object>> removedMap = new LinkedHashMap<String, LinkedList<Object>>();
					LinkedHashMap<Object, String> removed = new LinkedHashMap<Object, String>();
					if (!skipHeader()) {
						header = dataPerSheet.remove(0);
						normalizeHeader(removed, removedMap, header);	
					}
					
					normalizeValue(removed, removedMap, dataPerSheet);
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
	
	public Map<String, LinkedList<Object>>  normalizeHeader(Map<Object, String> removed, Map<String, LinkedList<Object>> removedMap, LinkedHashMap<String, Object> dataHeader) {
		for (Entry<String, Object> entry : dataHeader.entrySet()) {
			if (entry.getValue() != null) {
				String value = entry.getValue().toString();
				
				if (value.contains(".")) {
					
					String[] values = value.split("\\.");
					LinkedList<Object> columns = removedMap.get(values[0]);
					if (columns == null) columns = new LinkedList<Object>();
					columns.add(values[1]);
					removedMap.put(values[0], columns);
					removed.put(entry.getKey(), values[0]);
				}					
			}	
		}
		removeHeader(removed, removedMap, dataHeader);
		return removedMap;
	}
	
	private void removeHeader(Map<Object, String> removed, Map<String, LinkedList<Object>> removedMap, LinkedHashMap<String, Object> dataHeader) {
		if (removed != null && removed.size() > 0) {
			
			for (Object rem : removed.keySet()) {
				dataHeader.remove(rem);
			}
			
			for (Entry<String, LinkedList<Object>> entry : removedMap.entrySet()) {
				LinkedHashMap<String, Object> removedHeader = new LinkedHashMap<String, Object>();
				
				int i = 0;
				for (Object column : entry.getValue()) {
					removedHeader.put(i+"", column);
					i++;
				}
				
				dataHeader.put(entry.getKey(), removedHeader);				
			}
		}
	}
	
	
	public static void main(String[] args) {
		String s = "";
		String[] a = s.split("\\.");
		System.out.println(a.length);
		System.out.println(a[0]);
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
	public List<Object> normalizeValue(Map<Object, String> removedMap, Map<String, LinkedList<Object>> removedMapDetail, LinkedHashMap<Integer, LinkedHashMap<String, Object>> dataPerSheet) {
		List<Object> removed = null;
		for (Map<String, Object> data : dataPerSheet.values()) {
			removed = new ArrayList<Object>(removedMap.keySet());
			Map<String, Integer> arraySize = new HashMap<String, Integer>();
			
			// detect multiple value
			Map<String, LinkedList<String[]>> arrayList = new LinkedHashMap<String, LinkedList<String[]>>();
			for (Entry<String, Object> entry : data.entrySet()) {					
				if (removed.contains(entry.getKey())) {
					LinkedList<String[]> list = arrayList.get(removedMap.get(entry.getKey()));
					if (list == null) list = new LinkedList<String[]>();
					
					if (entry.getValue() != null) {
						String value = entry.getValue().toString();	
						String[] values = value.split("\\|");
						arraySize.put(removedMap.get(entry.getKey()), values.length);
						list.add(values);
					} else {
						arraySize.put(removedMap.get(entry.getKey()), 0);
					}
					arrayList.put(removedMap.get(entry.getKey()), list);
				}
			}
			
			for (Entry<String, LinkedList<String[]>> values : arrayList.entrySet()) {
				// normalisasi matrix
				LinkedHashMap<Integer, LinkedHashMap<String, Object>> normalize = new LinkedHashMap<Integer, LinkedHashMap<String, Object>>();
				for (int i=0; i<arraySize.get(values.getKey()); i++) {
					normalize.put(i, new LinkedHashMap<String, Object>());
				}
				
				// transpose matrix
				int z = 0;
				for (String[] arr : arrayList.get(values.getKey())) {
					for (int i=0; i< arraySize.get(values.getKey()); i++) {
						Map<String, Object> d = normalize.get(i);
						d.put(z+"", arr[i]);
					}
					z++;
				}
				
				// get index of removed object from value
				removed = new ArrayList<Object>();
				for (Entry<Object, String> removedEntry : removedMap.entrySet()) {
					if (removedEntry.getValue().equals(values.getKey())) 
						removed.add(removedEntry.getKey());
				}
				
				// remove datasheet
				for (Object rem : removed) {
					data.remove(rem);
				}
				
				// replace datasheet to new matrix
				data.put(values.getKey(), normalize.values());
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
		currentRow = dataCompile.removeFirst();
		return currentRow;
	}
	
	@Override
	public void close() {
		dataCompile.clear();
		data.clear();
		header.clear();
		container.clear();
	}
	
}
