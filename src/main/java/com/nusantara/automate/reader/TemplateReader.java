package com.nusantara.automate.reader;

import java.io.File;

public class TemplateReader {

	private BufferedFileReader fileReader;
	
	public TemplateReader(File file) {
		fileReader = new BufferedFileReader(file);
	}
	
	public StringBuffer read() {
		StringBuffer sb = new StringBuffer();
		while(fileReader.iterate()) {
			sb.append(fileReader.read()+System.lineSeparator());
		}
		fileReader.close();
		return sb;
	}

}
