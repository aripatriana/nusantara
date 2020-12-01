package com.nusantara.automate.reader;

import java.util.LinkedHashMap;

/**
 * Base class for read sheet in excel file
 * 
 * @author ari.patriana
 *
 * @param <T>
 */
public class XlsSheetReader<T> {

	XlsRowReader<T> rw;
	public XlsSheetReader(XlsRowReader<T> rw) {
		this.rw = rw;
	}
	
	public LinkedHashMap<Integer, T> readSheet(Boolean skipHeadRow) {
		int i = 0;
		LinkedHashMap<Integer, T> map = new LinkedHashMap<Integer, T>();
		while(rw.rowExists()) {
			if ((i == 0) && skipHeadRow) {
				rw.skipRow();
			} else {
				T t = rw.readRow();
				if (t != null) {
					map.put(i, t);
				}
			}
				
			i++;
		}
		return map;
	}
}
