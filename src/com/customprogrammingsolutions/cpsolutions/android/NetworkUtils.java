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

package com.customprogrammingsolutions.cpsolutions.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	
	public static String getPrettyByteCount(float bytes) {
		if(bytes < 1) {
			return "0 B";
		}
		if(bytes / 1024 < 1) {
			return bytes + " B";
		}
		bytes /= 1024;
		if(bytes / 1204 < 1) {
			return bytes + " kb";
		}
		bytes /= 1204;
		if(bytes / 1204 < 1) {
			return bytes + " mb";
		}
		bytes /= 1204;
		if(bytes / 1204 < 1) {
			return bytes + " gb";
		}
		bytes /= 1204;
		if(bytes / 1204 < 1) {
			return bytes + " tb";
		}
		bytes /= 1204;
		return bytes + " pb";
	}
}
