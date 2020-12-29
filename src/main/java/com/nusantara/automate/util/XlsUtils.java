package com.nusantara.automate.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.FileReader;
import com.nusantara.automate.reader.SimpleXlsFileReader;

public class XlsUtils {

	private static Logger log = LoggerFactory.getLogger(XlsUtils.class);
	
	public static Object getCellValue(Cell cell) {
		if (cell == null) return null;
		
		if (cell.getCellType() ==  Cell.CELL_TYPE_NUMERIC) {
			return cell.getNumericCellValue();
		}
		if (cell.getCellType() ==  Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().replace("\n", "").trim();
		}
		if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return null;
		}
		return null;
	}
	
	@SuppressWarnings("resource")
	public static void writeXls(File file, String[] headers, LinkedList<Map<String, Object>> data, int startRows, int startCols) {
		try {
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			
			// set header
			Row row = sheet.createRow(startRows);
			for (int j=startCols; j<headers.length+startCols; j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(headers[j-startCols]);
			}
			
			// set body
			int end = data.size()+startRows+1;
			for (int i=startRows+1; i<end; i++) {
				Row rb = sheet.createRow(i);
				Map<String, Object> map = data.removeFirst();
				
				for (int j=startCols; j<headers.length; j++) {
					Cell cell = rb.createCell(j);
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(String.valueOf(map.get(j-startCols+"")));
				}
			}
			
			workbook.write(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public static LinkedList<Map<String, Object>> readXls(File file) {
		FileReader<Map<String, Object>> fileReader = new SimpleXlsFileReader(file);
		LinkedList<Map<String, Object>> data = new LinkedList<Map<String,Object>>();

		while(fileReader.iterate()) {
			Map<String, Object> metadata = fileReader.read();
			data.add(metadata);
		}
		
		fileReader.close();
		return data;
	}
}
