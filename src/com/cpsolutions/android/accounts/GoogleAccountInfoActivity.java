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

package com.cpsolutions.android.accounts;

import java.io.IOException;

import com.cpsolutions.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GoogleAccountInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.google_account_info_layout);
    }

    @Override
    protected void onResume() {
            super.onResume();
            Intent intent = getIntent();
            AccountManager accountManager = AccountManager.get(getApplicationContext());
            if(intent.getExtras() == null) {
            	setResult(RESULT_CANCELED);
            	finish();
            	return;
            }
            Account account = (Account)intent.getExtras().get("account");
            if(account == null) {
            	setResult(RESULT_CANCELED);
            	finish();
            	return;
            }
            accountManager.getAuthToken(account, "ah", null, this, new GetAuthTokenCallback(), null);
    }
    
    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
		public void run(AccountManagerFuture<Bundle> result) {
			Bundle bundle = null;
			try {
				bundle = result.getResult();
				Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
				if(intent != null) {
					// User input required
					startActivity(intent);
				} else {
					onGetAuthToken(bundle);
				}
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);        
				finish();
			} catch (AuthenticatorException e) {
				e.printStackTrace();
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);        
				finish();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);        
				finish();
			}
		}
	};
	
	protected void onGetAuthToken(Bundle bundle) {
		String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
		Intent returnIntent = new Intent();
		returnIntent.putExtra("result", authToken);
		setResult(RESULT_OK, returnIntent);     
		finish();
	}
	
}
