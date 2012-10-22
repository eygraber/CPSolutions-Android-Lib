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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;

public class ListDialogMultiSelectFragment extends AbstractListDialogFragment implements ConfirmationDialogFragment.OnPositiveButtonClickListener{
	private String separator;
	private boolean[] mClickedDialogEntryIndices;
	private String valuesToRestore;
	
	private OnResultReadyListener rrl;
	
	public static ListDialogMultiSelectFragment newInstance(String title, String positiveButtonLabel, String negativeButtonLabel, String separator, String[] entries, String[] entryValues, String valuesToRestore) {
		ListDialogMultiSelectFragment dialog = new ListDialogMultiSelectFragment();
		Bundle args = new Bundle();
		args.putString(TITLE_EXTRA, title);
		args.putString(POSITIVE_BUTTON_LABEL_EXTRA, positiveButtonLabel);
		args.putString(NEGATIVE_BUTTON_LABEL_EXTRA, negativeButtonLabel);
		args.putStringArray(ENTRIES_EXTRA, entries);
		args.putStringArray(ENTRY_VALUES_EXTRA, entryValues);
		args.putString("separator", separator);
		if(valuesToRestore == null) {
			valuesToRestore = "";
		}
		args.putString("valuesToRestore", valuesToRestore);
		dialog.setArguments(args);
		
		return dialog;
	}
	
	public ListDialogMultiSelectFragment() {
		// Empty constructor required for DialogFragment
	}
	
	@Override
	public void setOnPositiveClickButtonListener(OnPositiveButtonClickListener pbcl) {
		//override this because we don't want anyone using it
	}
	
	@Override
	public void setOnNegativeClickButtonListener(OnNegativeButtonClickListener nbcl) {
		//override this because we don't want anyone using it
	}
	
	@Override
	protected void onDialogBuildFinished(Builder builder) {
		Bundle args = getArguments();
		separator = args.getString("separator");
		entries = args.getStringArray(ENTRIES_EXTRA);
		entryValues = args.getStringArray(ENTRY_VALUES_EXTRA);
		valuesToRestore = args.getString("valuesToRestore");
		
		this.pbcl = this;
		
		builder.setTitle(title);
		builder.setMessage(null);
		
		// Initialize the array of boolean to the same size as number of entries
        mClickedDialogEntryIndices = new boolean[entries.length];
        if(entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException("ListDialogMultiSelectFragment requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices, new DialogInterface.OnMultiChoiceClickListener() {
        			
        			@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {				
						mClickedDialogEntryIndices[which] = isChecked;
					}
        });
	}

	@Override
	public void onClick(DialogInterface dialog) {
		ArrayList<String> values = new ArrayList<String>();
        if(entryValues != null) {
        	for(int i = 0; i < entryValues.length; i++) 
        		if(mClickedDialogEntryIndices[i] == true)
        			values.add((String) entryValues[i]);
        
        	if(rrl != null) {
        		rrl.onResultReady(join(values, separator));
        	}            
        }
	}
	
	public String[] parseStoredValue(String val) {
		if("".equals(val)) 
			return null;
		else 
			return val.split(separator);
    }
	
	private void restoreCheckedEntries() {
    	List<String> vals = null;
    	
    	if(valuesToRestore != null && valuesToRestore.trim().compareTo("") != 0) {
    		vals = Arrays.asList(parseStoredValue(valuesToRestore));
    	}
    	
    	if(vals != null) 
        	for(int i = 0; i < entryValues.length; i++ ) 
            	if(vals.contains(entryValues[i])) 
        			mClickedDialogEntryIndices[i] = true;
    }

	
	public void setOnResultReadyListener(OnResultReadyListener rrl) {
		this.rrl = rrl;
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
