package com.nusantara.automate.reader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public abstract class XlsRowReader<T> {
	Iterator<Row> iterator;
	
	public XlsRowReader(Sheet sheet) throws FileNotFoundException, IOException {
        iterator = sheet.iterator();
	}
	
	public boolean rowExists() {
		return iterator.hasNext();
	}
	
	public abstract T readRow(Row currentRow);
	
	public T readRow() {
        return readRow(iterator.next());
	}
	
	public void skipRow() {
		iterator.next();
	}
}
