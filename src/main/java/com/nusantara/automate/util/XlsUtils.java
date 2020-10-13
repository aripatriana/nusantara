package com.nusantara.automate.util;

import org.apache.poi.ss.usermodel.Cell;

public class XlsUtils {

	
	public static Object getCellValue(Cell cell) {
		if (cell == null) return null;
		
		if (cell.getCellType() ==  Cell.CELL_TYPE_NUMERIC) {
			return cell.getNumericCellValue();
		}
		if (cell.getCellType() ==  Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().replace("\n", "");
		}
		if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return null;
		}
		return null;
	}
}
