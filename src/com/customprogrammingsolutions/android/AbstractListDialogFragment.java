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

public class AbstractListDialogFragment extends ConfirmationDialogFragment {
	protected static String ENTRIES_EXTRA = "entries";
	protected static String ENTRY_VALUES_EXTRA = "entryValues";
	public interface OnResultReadyListener {
		public void onResultReady(String result);
	}
	
	protected OnResultReadyListener rrl;
	

	protected String[] entries;
	protected String[] entryValues;
	
	public void setOnResultReadyListener(OnResultReadyListener rrl) {
		this.rrl = rrl;
	}
}
