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

import java.util.prefs.InvalidPreferencesFormatException;

import com.customprogrammingsolutions.cpsolutions.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {
	protected int mMaxValue = 100;
	protected int mMinValue = 0;
	protected int mDefaultValue = 50;
	protected int mInterval = 1;
	protected int mCurrentValue;
	protected String mUnitsLeft = "";
	protected String mUnitsRight = "";
	protected SeekBar mSeekBar;
	
	private float titleTextSize;
	private float summaryTextSize;
	private boolean useDefaultSizeForTitle = false;
	private boolean useDefaultSizeForSummary = false;
	
	private Context context;
	
	protected TextView mStatusText;

	public SeekBarPreference(Context context, AttributeSet attrs) throws InvalidPreferencesFormatException {
		super(context, attrs);
		init(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) throws InvalidPreferencesFormatException {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) throws InvalidPreferencesFormatException{	
		this.context = context;
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
		mMaxValue = a.getInt(R.styleable.SeekBarPreference_max, 100);
		mMinValue = a.getInt(R.styleable.SeekBarPreference_min, 0);
		
		mUnitsLeft = a.getString(R.styleable.SeekBarPreference_unitsLeft);
		if(mUnitsLeft == null)
			mUnitsLeft = "";
		mUnitsRight = a.getString(R.styleable.SeekBarPreference_unitsRight); 
		if(mUnitsRight == null)
			mUnitsRight = "";
		
		mInterval = a.getInt(R.styleable.SeekBarPreference_interval, mInterval);
		
		try{
			try{
				titleTextSize = setTextSize(context, a.getString(R.styleable.SeekBarPreference_title_text_size), true);
			}catch(NullTextSizeAttributeException ne){useDefaultSizeForTitle = true;}
			try{
				summaryTextSize = setTextSize(context, a.getString(R.styleable.SeekBarPreference_summary_text_size), false);
			}catch(NullTextSizeAttributeException ne){useDefaultSizeForSummary = true;}
		}catch(InvalidPreferencesFormatException e){
			throw e;
		}
		
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMaxValue - mMinValue);
		mSeekBar.setOnSeekBarChangeListener(this);
	}
	
	private float setTextSize(Context context, String textSizeString, boolean isTitle) throws InvalidPreferencesFormatException, NullTextSizeAttributeException{
		String pattern = "^\\d+(\\.\\d+|)(dp|px|dip){1}$";
		float density = context.getResources().getDisplayMetrics().density;
		
		if(textSizeString == null){
			throw new NullTextSizeAttributeException();
		}
		
		if(!textSizeString.matches(pattern))
			throw new InvalidPreferencesFormatException("Error: This type not allowed (at " + (isTitle ? "title_" : "summary_") + "text_size; acceptable units are dp, dip, or px)");
		
		float num = Float.parseFloat(textSizeString.replaceAll("(dp|px|dip){1}$", ""));
		String type = textSizeString.replaceAll("^\\d+(\\.\\d+|)", "");
		
		if(type == "px")
			return density / num;
		else
			return num;
	}
	
	public int getCurrentValue(){
		return mCurrentValue;
	}
	
	@Override
	protected View onCreateView(ViewGroup parent){
		RelativeLayout layout =  null;
		try {
			LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout = (RelativeLayout)mInflater.inflate(R.layout.seek_bar_preference, parent, false); 
			TextView title = (TextView)layout.findViewById(android.R.id.title);
			if(useDefaultSizeForTitle)
				title.setTextAppearance(context, android.R.attr.textAppearanceMedium);
			else
				title.setTextSize(titleTextSize);
			TextView summary = (TextView)layout.findViewById(android.R.id.summary);
			if(useDefaultSizeForSummary)
				summary.setTextAppearance(context, android.R.attr.textAppearanceSmall);
			else
				summary.setTextSize(summaryTextSize);
		}
		catch(Exception e){
			//AudioUtils.logThrowableIfDebug("onCreateView() - Error creating SeekBar for SeekBarPreference", Log.ERROR, e);
		}
		return layout;
	}
	
	@Override
	public void onBindView(View view){
		super.onBindView(view);

		try{
	        ViewParent oldContainer = mSeekBar.getParent();
	        ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);
	        
	        if(oldContainer != newContainer){
	            if(oldContainer != null){
	                ((ViewGroup) oldContainer).removeView(mSeekBar);
	            }
	            newContainer.removeAllViews();
	            newContainer.addView(mSeekBar, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	        }
		}
		catch(Exception e){
			//AudioUtils.logThrowableIfDebug("onBindView() - Error binding seekbar to view for SeekBarPreference", Log.ERROR, e);
		}

		updateView(view);
	}
    
	protected void updateView(View view){
		try {
			RelativeLayout layout = (RelativeLayout)view;

			mStatusText = (TextView)layout.findViewById(R.id.seekBarPrefValue);
			mStatusText.setText(String.valueOf(mCurrentValue));
			mStatusText.setMinimumWidth(30);
			
			mSeekBar.setProgress(mCurrentValue - mMinValue);

			TextView unitsRight = (TextView)layout.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);
			
			TextView unitsLeft = (TextView)layout.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(mUnitsLeft);
			
		}
		catch(Exception e){
			//AudioUtils.logThrowableIfDebug("updateView() - Error updating the seek bar view for SeekBarPreference", Log.ERROR, e);
		}
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int newValue = progress + mMinValue;
		
		if(newValue > mMaxValue)
			newValue = mMaxValue;
		else if(newValue < mMinValue)
			newValue = mMinValue;
		else if(mInterval != 1 && newValue % mInterval != 0)
			newValue = Math.round(((float)newValue)/mInterval)*mInterval;  

		// change accepted, store it
		mCurrentValue = newValue;
		if(fromUser)
			mStatusText.setText(String.valueOf(newValue));
		
		if(!callChangeListener(newValue)){
			return; 
		}
		
		persistInt(newValue);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar){}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar){
		notifyChanged();
	}


	@Override 
	protected Object onGetDefaultValue(TypedArray ta, int index){
		int defaultValue = ta.getInt(index, mDefaultValue);
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if(restoreValue) {
			mCurrentValue = getPersistedInt(mCurrentValue);
		}
		else{
			int temp = 0;
			try{
				temp = (Integer)defaultValue;
			}
			catch(Exception e){
				//AudioUtils.logIfDebug("onSetInitialValue() - Invalid default value for SeekBarPreference: " + defaultValue.toString(), Log.ERROR);
			}
			
			persistInt(temp);
			mCurrentValue = temp;
		}
		
	}
	
	private class NullTextSizeAttributeException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}
}
