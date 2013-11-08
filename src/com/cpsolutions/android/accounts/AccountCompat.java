package com.cpsolutions.android.accounts;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;

public class AccountCompat {
	@SuppressLint("NewApi")
	private void startGoogleAccountPickerActivity(Activity activity, int requestCode) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			activity.startActivityForResult(AccountManager.newChooseAccountIntent(null, null, new String[] {"com.google"}, false, null, null, null, null), requestCode);
    	}
    	else {
    		activity.startActivityForResult(new Intent(activity, GoogleAccountListActivity.class), requestCode);
    	}
	}
}
