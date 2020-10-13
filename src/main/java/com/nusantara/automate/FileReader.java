package com.nusantara.automate;
import java.io.File;

/**
 * The interface class for read file
 * 
 * @author ari.patriana
 *
 * @param <T>
 */
public interface FileReader<T> {

	public File getFile();
	
	public boolean skipHeader();
	
	public T getHeader();
	
	public boolean iterate();
	
	public T read();
	
	public void close();
}
