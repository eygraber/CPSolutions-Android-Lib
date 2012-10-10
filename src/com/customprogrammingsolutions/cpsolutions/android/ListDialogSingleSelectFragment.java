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

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ListDialogSingleSelectFragment extends AbstractListDialogFragment implements ConfirmationDialogFragment.OnPositiveButtonClickListener {
	private int valueToCheck;
	private String checkedValue;
	
	public static ListDialogSingleSelectFragment newInstance(String title, String positiveButtonLabel, String negativeButtonLabel, String[] entries, String[] entryValues, int valueToCheck) {
		ListDialogSingleSelectFragment dialog = new ListDialogSingleSelectFragment();
		Bundle args = new Bundle();
		args.putString(TITLE_EXTRA, title);
		args.putString(POSITIVE_BUTTON_LABEL_EXTRA, positiveButtonLabel);
		args.putString(NEGATIVE_BUTTON_LABEL_EXTRA, negativeButtonLabel);
		args.putStringArray(ENTRIES_EXTRA, entries);
		args.putStringArray(ENTRY_VALUES_EXTRA, entryValues);
		args.putInt("valueToCheck", valueToCheck);
		dialog.setArguments(args);
		
		return dialog;
	}
	
	public ListDialogSingleSelectFragment() {
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
		entries = args.getStringArray(ENTRIES_EXTRA);
		entryValues = args.getStringArray(ENTRY_VALUES_EXTRA);
		valueToCheck = args.getInt("valueToCheck");
		
		this.pbcl = this;
		
		builder.setTitle(title);
		builder.setMessage(null);
		
        if(entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException("ListDialogSingleSelectFragment requires an entries array and an entryValues array which are both the same length");
        }

        builder.setSingleChoiceItems(entries, valueToCheck, new OnClickListener() {
        			
        			@Override
					public void onClick(DialogInterface dialog, int which) {	
						checkedValue = entryValues[which];
					}
        });
	}

	@Override
	public void onClick(DialogInterface dialog) {
        if(rrl != null) {
        	rrl.onResultReady(checkedValue);
        }            
	}

}
