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

package com.cpsolutions.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.customprogrammingsolutions.cpsolutions.R;

public class GoogleAccountListActivity extends ListActivity {
    protected AccountManager accountManager;
    protected Intent intent;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	TextView tv = new TextView(this);
    	tv.setText("Please Select an Account");
    	tv.setPadding(5, 5, 0, 20);
    	getListView().addHeaderView(tv);
    	accountManager = AccountManager.get(getApplicationContext());
    	Account[] accounts = accountManager.getAccountsByType("com.google");
    	this.setListAdapter(new AccountArrayAdapter(this, R.layout.google_account_list_item, R.id.accountName, accounts));        
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
            Account account = (Account)getListView().getItemAtPosition(position);
            Intent intent = new Intent(this, GoogleAccountInfoActivity.class);
            intent.putExtra("account", account);
            startActivityForResult(intent, 1);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == 1 && resultCode == RESULT_OK) {
    		String authToken = data.getStringExtra("result");
    		Intent returnIntent = new Intent();
    		returnIntent.putExtra("result", authToken);
    		setResult(RESULT_OK, returnIntent);     
    		finish();
    	}
    	else {
    		Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);        
			finish();
    	}
    }
    
    private class AccountArrayAdapter extends ArrayAdapter<Account> {
    	
    	private LayoutInflater mInflater;
    	private int layoutResource;
    	private int textViewId;

		public AccountArrayAdapter(Context context, int resource, int textViewId, Account[] objects) {
			super(context, resource, textViewId, objects);
			layoutResource = resource;
			this.textViewId = textViewId;
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
    	public View getView(int position, View convertView, ViewGroup parent) {	
			final ViewHolder holder;
			
			String accountName = this.getItem(position).name;

	        if (convertView == null) {
	            convertView = mInflater.inflate(layoutResource, null);
	            TextView tv = (TextView)convertView.findViewById(textViewId);
	            tv.setText(accountName);
	            holder = new ViewHolder();
	            holder.accountName = tv;
	            convertView.setTag(holder);
	        } 
	        else{
	        	holder = (ViewHolder) convertView.getTag();
	        	holder.accountName.setText(accountName);
	        }
	        
	        return convertView;
    	}
    }
    
    static class ViewHolder {
        TextView accountName;
    }
}