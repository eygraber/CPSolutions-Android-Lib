package com.cpsolutions.android.utils;

import java.io.File;
import java.io.IOException;

/**
 * Utility methods for dealing with files.
 * 
 * @author eygraber
 *
 */
public class FileUtils {
	/**
	 * Returns a unique filename for the provided filename. If the filename does not exist, it returns that
	 * filename. If the file does exist and has an extension it returns the filename with a number in front
	 * of the .extension. If it exists but doesn't have an extension, it returns the filename with a number
	 * appended to the end of it. This method throws an <code>IllegalArgumentException</code> if 
	 * <code>filename</code> is empty, ".", or "..".
	 * This method will run in an infinite loop if it never finds a unique filename.
	 * 
	 * @param filename a <code>String</code> representation of the filename.
	 * 
	 * @return a <code>String</code> with a unique filename based on the <code>filename</code> parameter..
	 * 
	 * @throws IllegalArgumentException
	 */
	public static String getUniqueFilename(String filename) throws IllegalArgumentException {
		return getUniqueFilename(filename, Integer.MIN_VALUE);
	}
	
	/**
	 * Returns a unique filename for the provided filename. If the filename does not exist, it returns that
	 * filename. If the file does exist and has an extension it returns the filename with a number in front
	 * of the .extension. If it exists but doesn't have an extension, it returns the filename with a number
	 * appended to the end of it. This method throws an <code>IllegalArgumentException</code> if 
	 * <code>filename</code> is empty, ".", "..", or if retries is less than 0.
	 * 
	 * @param filename a <code>String</code> representation of the filename.
	 * @param retries how many times should we try to find a unique filename.
	 * 
	 * @return a <code>String</code> with a unique filename based on the <code>filename</code> parameter, or null if we need to try more than retries times.
	 * 
	 * @throws IllegalArgumentException
	 */
	public static String getUniqueFilename(String filename, int retries) throws IllegalArgumentException {
		filename = filename.trim();
		if(filename.length() == 0 || filename == "." || filename == "..") {
			throw new IllegalArgumentException("The filename must not be empty, ., or ..");
		}
		if(retries < 0 && retries != Integer.MIN_VALUE) {
			throw new IllegalArgumentException("retries must be greater than or equal to 0");
		}
		int count = 0;
		File f = null;
		String newFilename = filename;
		while(retries != Integer.MIN_VALUE && (count - 1) < retries) {
			f = new File(newFilename);
			if(!f.exists()) {
				return newFilename;
			}
			
			int lastDot = filename.lastIndexOf(".");
			if(filename.endsWith(".") || lastDot == 0) {
				lastDot = -1;
			}
			
			count++;
			if(lastDot < 0) {
				newFilename = filename + count;
			}
			else {
				newFilename = filename.substring(0, lastDot) + count + filename.substring(lastDot);
			}
		}
		return null;
	}
	
	/**
	 * Creates a <code>File</code> with the given filename. If the file already exists and has an extension
	 * it returns the filename with a number in front of the .extension. If it exists but doesn't have an
	 * extension, it returns the filename with a number appended to the end of it. This method throws
	 * an <code>IllegalArgumentException</code> if <code>filename</code> is empty, ".", ".."..
	 * This method will run in an infinite loop if it never finds a unique filename.
	 * 
	 * @param filename a <code>String</code> representation of the filename.
	 * 
	 * @return a <code>File</code> with a unique filename based on the <code>filename</code> parameter.
	 * 
	 * @throws IllegalArgumentException, IOException 
	 */
	public static File createFileWithUniqueFilename(String filename) throws IllegalArgumentException, IOException {
		return createFileWithUniqueFilename(filename, Integer.MIN_VALUE);
	}
	
	/**
	 * Creates a <code>File</code> with the given filename. If the file already exists and has an extension
	 * it returns the filename with a number in front of the .extension. If it exists but doesn't have an
	 * extension, it returns the filename with a number appended to the end of it. This method throws
	 * an <code>IllegalArgumentException</code> if <code>filename</code> is empty, ".", "..", or if retries if less than 0.
	 * 
	 * @param filename a <code>String</code> representation of the filename.
	 * @param retries how many times should we try to find a unique filename.
	 * 
	 * @return a <code>File</code> with a unique filename based on the <code>filename</code> parameter, or null if we need to try more than retries times.
	 * 
	 * @throws IllegalArgumentException, IOException 
	 */
	public static File createFileWithUniqueFilename(String filename, int retries) throws IllegalArgumentException, IllegalArgumentException, IOException {
		filename = filename.trim();
		if(filename.length() == 0 || filename == "." || filename == "..") {
			throw new IllegalArgumentException("The filename must not be empty, ., or ..");
		}
		if(retries < 0 && retries != Integer.MIN_VALUE) {
			throw new IllegalArgumentException("retries must be greater than or equal to 0");
		}
		int count = 0;
		File f = null;
		String newFilename = filename;
		while(retries != Integer.MIN_VALUE && (count - 1) < retries) {
			f = new File(newFilename);
			if(f.createNewFile()) {
				return f;
			}
			
			int lastDot = filename.lastIndexOf(".");
			if(filename.endsWith(".") || lastDot == 0) {
				lastDot = -1;
			}
			
			count++;
			if(lastDot < 0) {
				newFilename = filename + count;
			}
			else {
				newFilename = filename.substring(0, lastDot) + count + filename.substring(lastDot);
			}
		}
		
		return null;
	}
}
