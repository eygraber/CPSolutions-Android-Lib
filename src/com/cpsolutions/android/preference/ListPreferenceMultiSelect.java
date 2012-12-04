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

package com.cpsolutions.android.preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.customprogrammingsolutions.cpsolutions.R;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceMultiSelect extends ListPreference {
	private String separator;
	private String valuesToRestore = "";
	private static String DEFAULT_SEPARATOR = ";";
	private boolean[] mClickedDialogEntryIndices;

	public ListPreferenceMultiSelect(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceMultiSelect);
        String s = a.getString(R.styleable.ListPreferenceMultiSelect_separator );
        if(s != null) 
        	separator = s;
        else 
        	separator = DEFAULT_SEPARATOR;
        
     // Initialize the array of boolean to the same size as number of entries
        mClickedDialogEntryIndices = new boolean[getEntries().length];
    }
	
	@Override
    public void setEntries(CharSequence[] entries) {
    	super.setEntries(entries);
    	// Initialize the array of boolean to the same size as number of entries
        mClickedDialogEntryIndices = new boolean[entries.length];
    }
	
	public void setValuesToRestore(String valuesToRestore) {
		this.valuesToRestore = valuesToRestore;
	}
    
    public ListPreferenceMultiSelect(Context context) {
        this(context, null);
    }
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
    	CharSequence[] entries = getEntries();
    	CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries(entryValues);
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices, new DialogInterface.OnMultiChoiceClickListener() {
        			
        			@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {				
						mClickedDialogEntryIndices[which] = isChecked;
					}
        });
    }

    public String[] parseStoredValue(String val) {
    	try {
    		if("".equals(val)) {
    			return null;
    		}
    		else { 
    			return val.split(separator);
    		}
    	} catch(Exception e) {
    		return null;
    	}
		
    }
    
    private void restoreCheckedEntries(CharSequence[] entryValues) {
    	List<String> vals = null;   	
    	if(valuesToRestore != null && valuesToRestore.trim().compareTo("") != 0) {
    		vals = Arrays.asList(parseStoredValue(valuesToRestore));
    	}
    	
    	if(vals != null) 
        	for(int i = 0; i < entryValues.length; i++ ) 
            	if(vals.contains(entryValues[i])) 
        			mClickedDialogEntryIndices[i] = true;
    }

	@Override
    protected void onDialogClosed(boolean positiveResult) {
//        super.onDialogClosed(positiveResult);
		ArrayList<String> values = new ArrayList<String>();
        
    	CharSequence[] entryValues = getEntryValues();
        if(positiveResult && entryValues != null) {
        	for(int i = 0; i < entryValues.length; i++) 
        		if(mClickedDialogEntryIndices[i] == true)
        			values.add((String) entryValues[i]);
        
            if(callChangeListener(join(values, separator))) 
        		setValue(join(values, separator));
            
        }
    }
	
	// Credits to kurellajunior on this post http://snippets.dzone.com/posts/show/91
	protected static String join( Iterable< ? extends Object > pColl, String separator) {
        Iterator< ? extends Object > oIter;
        if(pColl == null || (!(oIter = pColl.iterator()).hasNext()))
            return "";
        StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
        while(oIter.hasNext())
            oBuilder.append(separator).append(oIter.next());
        return oBuilder.toString();
    }
}
