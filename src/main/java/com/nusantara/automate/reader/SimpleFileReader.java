package com.nusantara.automate.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.nusantara.automate.FileReader;

public class SimpleFileReader implements FileReader<String> {

	private File file;
	private Scanner scanner;
	public SimpleFileReader(File file) {
		this.file = file;
		try {
			 scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
	public String getHeader() {
		return null;
	}

	@Override
	public boolean iterate() {
		if (scanner == null) return false;
		return scanner.hasNextLine();
	}

	@Override
	public String read() {
		if (scanner == null) return null;
		return scanner.nextLine();
	}
	
	@Override
	public void close() {
		scanner.close();
	}

}
