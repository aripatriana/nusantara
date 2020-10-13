package com.nusantara.automate;
import java.io.File;

public interface FileReader<T> {

	public File getFile();
	
	public boolean skipHeader();
	
	public T getHeader();
	
	public boolean iterate();
	
	public T read();
	
	public void close();
}
