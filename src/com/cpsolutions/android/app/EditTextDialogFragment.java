package com.cpsolutions.android.app;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

public class EditTextDialogFragment extends ConfirmationDialogFragment implements ConfirmationDialogFragment.OnPositiveButtonClickListener {
	private EditText editText;
	private static String INITIAL_VALUE_EXTRA = "intialValue";
	
	public static EditTextDialogFragment newInstance(String title) {
		return newInstance(title, "Ok", "Cancel", "");
	}
	
	public static EditTextDialogFragment newInstance(String title, String initialValue) {
		return newInstance(title, "Ok", "Cancel", initialValue);
	}
	
	public static EditTextDialogFragment newInstance(String title, String positiveButtonLabel, String negativeButtonLabel, String initialValue) {
		EditTextDialogFragment dialog = new EditTextDialogFragment();
		Bundle args = new Bundle();
		args.putString(TITLE_EXTRA, title);
		args.putString(POSITIVE_BUTTON_LABEL_EXTRA, positiveButtonLabel);
		args.putString(NEGATIVE_BUTTON_LABEL_EXTRA, negativeButtonLabel);
		args.putString(INITIAL_VALUE_EXTRA, (initialValue == null ? "" : initialValue));
		dialog.setArguments(args);
		
		return dialog;
	}
	
	public EditTextDialogFragment() {
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
		
		this.pbcl = this;
		
		builder.setTitle(title);
		builder.setMessage(null);
		
		editText = new EditText(getActivity());
		editText.setText(args.getString(INITIAL_VALUE_EXTRA));
		editText.setInputType(InputType.TYPE_CLASS_TEXT);
		
		builder.setView(editText);
	}

	@Override
	public void onClick(DialogInterface dialog) {
		if(rrl != null) {
        	rrl.onResultReady(editText.getText().toString());
        }  
	}

}
