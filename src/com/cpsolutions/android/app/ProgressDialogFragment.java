package com.cpsolutions.android.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class ProgressDialogFragment extends DialogFragment {
	public ProgressDialogFragment() {}
	
	public static ProgressDialogFragment show(Context context, FragmentManager manager, int titleRes, int messageRes) {
		return show(manager, context.getString(titleRes), context.getString(messageRes));
	}
	
	public static ProgressDialogFragment show(FragmentManager manager, String title, String message) {
		ProgressDialogFragment frag = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		frag.setArguments(args);
		try {
			manager.beginTransaction().add(frag, "dialog").commitAllowingStateLoss();
		} catch(Exception e){}
		//frag.show(manager, "dialog");
		return frag;
	}
	
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
	    final ProgressDialog dialog = new ProgressDialog(getActivity());
	    dialog.setTitle(getArguments().getString("title"));
	    dialog.setMessage(getArguments().getString("message"));
	    dialog.setIndeterminate(true);
	    dialog.setCancelable(false);
	    setCancelable(false);
	    return dialog;
	}
}