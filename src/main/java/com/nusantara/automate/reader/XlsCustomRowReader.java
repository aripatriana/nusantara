package com.nusantara.automate.reader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.nusantara.automate.util.MapUtils;
import com.nusantara.automate.util.XlsUtils;


/**
 * The implementation for read a row of excel
 * 
 * @author ari.patriana
 *
 */
public class XlsCustomRowReader extends XlsRowReader<LinkedHashMap<String, Object>> {

	public XlsCustomRowReader(Sheet sheet) throws FileNotFoundException,
			IOException {
		super(sheet);
	}
	
	@Override
	public LinkedHashMap<String, Object> readRow(Row currentRow) {
		LinkedHashMap<String, Object> dataPerRow = new LinkedHashMap<String, Object>();
		int lastColumnIndex = currentRow.getLastCellNum();
		for (int index = 0; index<lastColumnIndex; index++) {
			dataPerRow.put(index+"", XlsUtils.getCellValue(currentRow.getCell(index)));
		}
		
		if (MapUtils.checkAllNull(dataPerRow))
			return null;
		
		return dataPerRow;
	}

}
