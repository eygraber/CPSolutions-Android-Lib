package com.cpsolutions.android.utils;

/**
 * A simple class that holds a name value pair (useful for storing HTTP headers and POST parameters).
 * @author eygraber
 *
 */
public class NameValuePair {
	/**
	 * The name of the pair.
	 */
	protected String name;
	
	/**
	 * The value of the pair.
	 */
	protected String value;
	
	/**
	 * Class constructor specifying the name and value.
	 * 
	 * @param name the name.
	 * 
	 * @param value the value.
	 */
	public NameValuePair(String name, String value) {
		if(name == null) {
			throw new IllegalArgumentException("name must not be null");
		}
		if(value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Returns the name of the pair.
	 * 
	 * @return the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the value of the pair.
	 * 
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}
}
