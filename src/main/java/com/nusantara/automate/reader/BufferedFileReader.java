package com.nusantara.automate.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nusantara.automate.FileReader;

public class BufferedFileReader implements FileReader<String> {

	private Logger log = LoggerFactory.getLogger(SimpleFileReader.class);
	
	private File file;
	private BufferedReader buffReader;
	private int size;
	private String cached;
	
	public BufferedFileReader(File file) {
		this.file = file;
		size= 0;
		try {
			buffReader = new BufferedReader(new java.io.FileReader(file));
		} catch (FileNotFoundException e) {
			log.error("ERROR ", e);
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
		try {
			cached = buffReader.readLine();
		} catch (IOException e) {
			return false;
		}
		return cached != null;
	}

	@Override
	public String read() {
		size++;
		return cached;
	}
	
	@Override
	public void close() {
		try {
			if (buffReader != null)
				buffReader.close();
	   } catch (IOException e) {
		   log.error("ERROR ", e);
	   }
	}
	
	@Override
	public int getSize() {
		return size;
	}
}