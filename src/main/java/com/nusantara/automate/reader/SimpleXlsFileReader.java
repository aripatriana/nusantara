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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.FileReader;

public class SimpleXlsFileReader implements FileReader<Map<String, Object>> {

	private Logger log = LoggerFactory.getLogger(SimpleXlsFileReader.class);
	
	protected LinkedList<Map<String, Object>> data = new LinkedList<Map<String,Object>>();
	protected LinkedList<Map<String, Object>> dataCompile;
	protected Map<String, Object> currentRow;
	private Workbook workbook;
	private File file;
	private int size;
	
	public SimpleXlsFileReader(File file) {
		this.file = file;
		try {
			workbook = new XSSFWorkbook(new FileInputStream(file));
			Sheet sheet = workbook.getSheetAt(0);
			XlsSheetReader<LinkedHashMap<String, Object>> dataSheet = new XlsSheetReader<LinkedHashMap<String, Object>>(new XlsCustomRowReader(sheet));
			LinkedHashMap<Integer, LinkedHashMap<String, Object>> dataPerSheet = dataSheet.readSheet(skipHeader());
			data.addAll(dataPerSheet.values());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
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
		return new HashMap<String, Object>();
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
		
	}

	@Override
	public int getSize() {
		return size;
	}

}
