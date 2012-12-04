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

package com.cpsolutions.android.content;

import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SyncedSharedPreferences {
	//TODO
	//for all methods that call aquire()
	//if aquire() returns false throw an exception
	//handle it in AlarmPreferenceManager by trying until works or timeout
	private final static SyncedSharedPreferences me = new SyncedSharedPreferences();
	
	private SharedPreferences prefs;
	private static Semaphore mutex;
	
	public static SyncedSharedPreferences getSyncedPrefManager(){
		return me;
	}
	
	private SyncedSharedPreferences(){
    	mutex = new Semaphore(1, true);
	}
	
	/*
	 5/20/2012
	 Changed visibility to private...no errors as of yet but watch out for it
	 */
	private SharedPreferences getPref(Context caller){
		if(prefs == null)
			prefs = PreferenceManager.getDefaultSharedPreferences(caller);
		return prefs;
	}
	
	public boolean aquireLock(){
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public boolean releaseLock(){
		mutex.release();
		return true;
	}
	
	public String getString(String preference, String failedVal, Context caller){
    	aquireLock();
    	String val = getPref(caller).getString(preference, failedVal);
    	releaseLock();
    	return val;
    }
    
    public int getInt(String preference, int failedVal, Context caller){
    	aquireLock();
    	int val = getPref(caller).getInt(preference, failedVal);
    	releaseLock();
    	return val;
    }
    
    public long getLong(String preference, long failedVal, Context caller){
    	aquireLock();
    	long val = getPref(caller).getLong(preference, failedVal);
    	releaseLock();
    	return val;
    }
    
    public boolean getBoolean(String preference, boolean failedVal, Context caller){
    	aquireLock();
    	boolean val = getPref(caller).getBoolean(preference, failedVal);
    	releaseLock();
    	return val;
    }
    
    public void setString(String preference, String val, Context caller){
    	aquireLock();
    	getPref(caller).edit().putString(preference, val).commit();
    	releaseLock();
    }
    
    public void setInt(String preference, int val, Context caller){
    	aquireLock();
    	getPref(caller).edit().putInt(preference, val).commit();
    	releaseLock();
    }
    
    public void setLong(String preference, long val, Context caller){
    	aquireLock();
    	getPref(caller).edit().putLong(preference, val).commit();
    	releaseLock();
    }
    
    public void setBoolean(String preference, boolean val, Context caller){
    	aquireLock();
    	getPref(caller).edit().putBoolean(preference, val).commit();
    	releaseLock();
    }
    
    public boolean exists(String preference, Context caller){
    	aquireLock();
    	boolean val = getPref(caller).contains(preference);
    	releaseLock();
    	return val;
    }
    
    public void remove(String preference, Context caller){
    	aquireLock();
    	getPref(caller).edit().remove(preference).commit();
    	releaseLock();
    }

}

