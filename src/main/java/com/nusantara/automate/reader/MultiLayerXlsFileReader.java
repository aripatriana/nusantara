package com.nusantara.automate.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.FileReader;
import com.nusantara.automate.exception.XlsSheetStyleException;
import com.nusantara.automate.util.MapUtils;

public class MultiLayerXlsFileReader extends MadnessXlsFileReader implements FileReader<Map<String, Object>> {

	private Logger log = LoggerFactory.getLogger(MultiLayerXlsFileReader.class);
	
	private static final String MULTI_LAYER_DISTINCTION = "multi_layer_distinction";
	private static final String NO_DISTINCTION_FOR_ALL_LAYER = "no_distinction_for_all_layer";
	private Map<String, LinkedHashMap<String, Object>> multiHeader = new HashMap<String, LinkedHashMap<String,Object>>();
	private LinkedList<String> sheetList = new LinkedList<String>();
	
	public MultiLayerXlsFileReader(File file) throws XlsSheetStyleException {
		this.file = file;
		try {
			workbook = new XSSFWorkbook(new FileInputStream(file));
			activeSheet = workbook.getNumberOfSheets();
			
			Map<String, Map<Integer, LinkedList<Map<String, Object>>>> sheetContainer = new HashMap<String, Map<Integer,LinkedList<Map<String,Object>>>>(); 
			String styleSheetFormat = NO_DISTINCTION_FOR_ALL_LAYER;
			
			
			// verify standar style format
			for (int index = 0; index<activeSheet; index++) {
				Sheet sheet = workbook.getSheetAt(index);
				
				if (!sheet.getSheetName().equalsIgnoreCase("meta-data")) {

					if (index == 0) {
						styleSheetFormat = getStyleSheetFormat(sheet.getSheetName());
					} else {
						if (!getStyleSheetFormat(sheet.getSheetName()).equals(styleSheetFormat)) {
							throw new XlsSheetStyleException("Style sheet name is not uniform " + sheet.getSheetName());
						}
					}
					
					sheetList.add(sheet.getSheetName());
				}
			}
			
			// read and normalize data
			for (String sheetName : sheetList) {
				Sheet sheet = workbook.getSheet(sheetName);

				header = new LinkedHashMap<String, Object>();
				container = new HashMap<Integer, LinkedList<Map<String, Object>>>();

				XlsSheetReader<LinkedHashMap<String, Object>> dataSheet = new XlsSheetReader<LinkedHashMap<String, Object>>(new XlsCustomRowReader(sheet));
				LinkedHashMap<Integer, LinkedHashMap<String, Object>> dataPerSheet = dataSheet.readSheet(skipHeader());
				
				
				Map<String, LinkedList<Object>> removedMap = new LinkedHashMap<String, LinkedList<Object>>();
				LinkedHashMap<Object, String> removed = new LinkedHashMap<Object, String>();
				if (!skipHeader()) {
					header = dataPerSheet.remove(0);
					normalizeHeader(removed, removedMap, header);	
				}
				
				normalizeValue(removed, removedMap, dataPerSheet);
				container.put(0, new LinkedList<Map<String, Object>>(dataPerSheet.values()));
				
				sheetContainer.put(sheetName, container);
				multiHeader.put(sheetName.replace("$", ""), header);
			}
			
			// standarize key column format
			data = new LinkedList<Map<String, Object>>();
			if (styleSheetFormat.equals(MULTI_LAYER_DISTINCTION)) {
				for (String sheetName : sheetList) {
					Map<Integer, LinkedList<Map<String, Object>>> container = sheetContainer.get(sheetName);
					LinkedList<Map<String, Object>> newRowList = new LinkedList<Map<String,Object>>();
					for (LinkedList<Map<String, Object>> rowList : container.values()) {
						for (Map<String, Object> row : rowList) {
							
							MapUtils.concatMapKey(sheetName.replace("$", "") + ".", row);
							newRowList.add(row);
						}
					}
					data.addAll(newRowList);
				}				
			} else {
				for (LinkedList<Map<String, Object>> d : container.values()) {
					data.addAll(d);
				}				
			}
		} catch (FileNotFoundException e) {
			log.error("ERROR ", e);
		} catch (IOException e) {
			log.error("ERROR ", e);
		}
	}
	

	private String getStyleSheetFormat(String sheetName) {
		if (sheetName.startsWith("$")) {
			return MULTI_LAYER_DISTINCTION;
		} else {
			return NO_DISTINCTION_FOR_ALL_LAYER; 
		}
	}
	

	@Override
	public Map<String, Object> getHeader() {
		String key = (String) currentRow.keySet().toArray()[0];
		if (key.contains(".")) { 
			return multiHeader.get(key.split("\\.")[0]);
		}
		return super.getHeader();
	}

}
