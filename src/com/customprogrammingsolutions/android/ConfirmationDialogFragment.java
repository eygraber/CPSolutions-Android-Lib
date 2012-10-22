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

package com.customprogrammingsolutions.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmationDialogFragment extends DialogFragment {
	protected String title = "";
	protected String message = "";
	protected String positiveButtonLabel = "";
	protected String negativeButtonLabel = "";
	
	protected static String TITLE_EXTRA = "title";
	protected static String MESSAGE_EXTRA = "message";
	protected static String POSITIVE_BUTTON_LABEL_EXTRA = "positive_button_label";
	protected static String NEGATIVE_BUTTON_LABEL_EXTRA = "negative_button_label";
	
	public interface OnPositiveButtonClickListener {
		public void onClick(DialogInterface dialog);
	}
	
	public interface OnNegativeButtonClickListener {
		public void onClick(DialogInterface dialog);
	}
	
	protected OnPositiveButtonClickListener pbcl = null;
	protected OnNegativeButtonClickListener nbcl = null;
	
	public void setOnPositiveClickButtonListener(OnPositiveButtonClickListener pbcl) {
		this.pbcl = pbcl;
	}
	
	public void setOnNegativeClickButtonListener(OnNegativeButtonClickListener nbcl) {
		this.nbcl = nbcl;
	}
	
	public static ConfirmationDialogFragment newInstance(String title, String message, String positiveButtonLabel, String negativeButtonLabel) {
		ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_EXTRA, title);
        args.putString(MESSAGE_EXTRA, message);
        args.putString(POSITIVE_BUTTON_LABEL_EXTRA, positiveButtonLabel);
        args.putString(NEGATIVE_BUTTON_LABEL_EXTRA, negativeButtonLabel);
        dialog.setArguments(args);
        return dialog;
	}
	
	public ConfirmationDialogFragment() {
		// Empty constructor required for DialogFragment
	}
	
	protected void setPositiveButton(Builder builder) {
		builder.setPositiveButton(positiveButtonLabel,
			      new DialogInterface.OnClickListener() {
			  	  		public void onClick(DialogInterface dialog, int whichButton) {
			  	  			if(pbcl != null) {
			  	  				pbcl.onClick(dialog);
			  	  			}
			  	  			dialog.dismiss();
			  	  		}
  			  });
	}
	
	protected void setNegativeButton(Builder builder) {
		builder.setNegativeButton(negativeButtonLabel,
      		  new DialogInterface.OnClickListener() {
          			public void onClick(DialogInterface dialog, int whichButton) {
          				if(nbcl != null) {
          					nbcl.onClick(dialog);
          				}
          				dialog.dismiss();
          			}
				 });
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		setCancelable(false);
		
		title = getArguments().getString(TITLE_EXTRA);
		message = getArguments().getString(MESSAGE_EXTRA);
		positiveButtonLabel = getArguments().getString(POSITIVE_BUTTON_LABEL_EXTRA);
		negativeButtonLabel = getArguments().getString(NEGATIVE_BUTTON_LABEL_EXTRA);

        Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        setPositiveButton(builder);
        setNegativeButton(builder);
        onDialogBuildFinished(builder);
	    return builder.create();
	}

	protected void onDialogBuildFinished(Builder builder) {
	}
}
