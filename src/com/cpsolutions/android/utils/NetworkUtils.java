/*
 * Copyright 2012 Eliezer Graber (Custom Programming Solutions)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cpsolutions.android.utils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class NetworkUtils {
	public static boolean hasNetworkConnection(Context caller) {
	    ConnectivityManager cm = (ConnectivityManager) caller.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean isInCall(Context caller){
		return ((TelephonyManager) caller.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() != TelephonyManager.CALL_STATE_IDLE;	
	}
	
	/**
	 * Returns a <code>String</code> formatting the amount of bytes passed in, so that they are displayed in the 
	 * highest denomination of memory that keeps the value to greater than 0.
	 * 
	 * @param bytes the amount of bytes (as a long) we want to get a pretty <code>String</code> for.
	 * 
	 * @return a pretty <code>String</code> representing the amount of bytes passed in.
	 */
	public static String getPrettyByteCount(long bytes) {
		return getPrettyByteCount((double) bytes);
	}
	
	/**
	 * Returns a <code>String</code> formatting the amount of bytes passed in, so that they are displayed in the 
	 * highest denomination of memory that keeps the value to greater than 0.
	 * 
	 * @param bytes the amount of bytes (as a double) we want to get a pretty <code>String</code> for.
	 * 
	 * @return a pretty <code>String</code> representing the amount of bytes passed in.
	 */
	public static String getPrettyByteCount(double bytes) {
		if(bytes < 1) {
			return "0 B";
		}
		DecimalFormat form = new DecimalFormat("0.00"); 
		if(bytes / 1024 < 1) {
			return form.format(bytes) + " B";
		}
		bytes /= 1024;
		if(bytes / 1204 < 1) {
			return form.format(bytes) + " kb";
		}
		bytes /= 1024;
		if(bytes / 1024 < 1) {
			return form.format(bytes) + " mb";
		}
		bytes /= 1024;
		if(bytes / 1024 < 1) {
			return form.format(bytes) + " gb";
		}
		bytes /= 1024;
		if(bytes / 1024 < 1) {
			return form.format(bytes) + " tb";
		}
		bytes /= 1024;
		return form.format(bytes) + " pb";
	}
	
	/**
	 * Returns a URL encoded parameter string to be used in an HTTP POST or GET.
	 * 
	 * @param parameters a <code>List</code> of <code>NameValuePair</code> containing the parameters.
	 * 
	 * @return a URL encoded <code>String</code> of the form "param1=1&param2=2...".
	 */
	public static String getParameterString(List<NameValuePair> parameters, boolean shouldUrlEncode) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < parameters.size(); i++) {
			NameValuePair param = parameters.get(i);
			if(i > 0) {
				sb.append("&");
			}
			sb.append(param.getName()).append("=");
			if(shouldUrlEncode) {
				try {
					sb.append(URLEncoder.encode(param.getValue(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {}
			}
			else {
				sb.append(param.getValue());
			}
		}
		return sb.toString();
	}
	
	public static NetworkInterface getWifiNetworkInterface(Context context) {
		return getWifiNetworkInterface((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
	}
	
	public static NetworkInterface getWifiNetworkInterface(WifiManager manager) {
		Enumeration<NetworkInterface> interfaces = null;
	    try {
	        //the WiFi network interface will be one of these.
	        interfaces = NetworkInterface.getNetworkInterfaces();
	    } catch (SocketException e) {
	        return null;
	    }
	     
	    //We'll use the WiFiManager's ConnectionInfo IP address and compare it with
	    //the ips of the enumerated NetworkInterfaces to find the WiFi NetworkInterface.
	 
	    //Wifi manager gets a ConnectionInfo object that has the ipAdress as an int
	    //It's endianness could be different as the one on java.net.InetAddress
	    //maybe this varies from device to device, the android API has no documentation on this method.
	    int wifiIP = manager.getConnectionInfo().getIpAddress();
	     
	    //so I keep the same IP number with the reverse endianness
	    int reverseWifiIP = Integer.reverseBytes(wifiIP);       
	 
	    while (interfaces.hasMoreElements()) {
	 
	        NetworkInterface iface = interfaces.nextElement();
	 
	        //since each interface could have many InetAddresses...
	        Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
	        while (inetAddresses.hasMoreElements()) {
	            InetAddress nextElement = inetAddresses.nextElement();
	            int byteArrayToInt = byteArrayToInt(nextElement.getAddress(),0);
	             
	            //grab that IP in byte[] form and convert it to int, then compare it
	            //to the IP given by the WifiManager's ConnectionInfo. We compare
	            //in both endianness to make sure we get it.
	            if (byteArrayToInt == wifiIP || byteArrayToInt == reverseWifiIP) {
	                return iface;
	            }
	        }
	    }
	 
	    return null;
	}
	 
	public static final int byteArrayToInt(byte[] arr, int offset) {
	    if (arr == null || arr.length - offset < 4)
	        return -1;
	 
	    int r0 = (arr[offset] & 0xFF) << 24;
	    int r1 = (arr[offset + 1] & 0xFF) << 16;
	    int r2 = (arr[offset + 2] & 0xFF) << 8;
	    int r3 = arr[offset + 3] & 0xFF;
	    return r0 + r1 + r2 + r3;
	}
}
