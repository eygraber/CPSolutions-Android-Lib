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

package com.cpsolutions.android.widgets;

import com.cpsolutions.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class StandardSeekBar extends SeekBar implements OnSeekBarChangeListener {
	protected int mMinValue = 0;
	protected int mDefaultValue = 50;
	protected int mCurrentValue;
	protected String mUnit = "%";
	private TextView progressView = null;
	
	public interface OnValueChangeListener {
		public void onValueChange(int newValue);
	}
	private OnValueChangeListener vcl = null;
	
	public StandardSeekBar(Context context) {
		super(context);
	}
	
	public StandardSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StandardSeekBar, 0, 0);
		mMinValue = a.getInt(R.styleable.StandardSeekBar_sseek_bar_min, 0);
		
		setMax(a.getInt(R.styleable.StandardSeekBar_sseek_bar_max, 100));
		
		setProgress(a.getInt(R.styleable.StandardSeekBar_sseek_bar_start_at, getMax()));
		mCurrentValue = getProgress();
		incrementProgressBy(a.getInt(R.styleable.StandardSeekBar_sseek_bar_interval, 1));
		setOnSeekBarChangeListener(this);
		
		mUnit = a.getString(R.styleable.StandardSeekBar_sseek_bar_unit);
		if(mUnit == null) {
			mUnit = "%";
		}
		
		a.recycle();
	}
	
	public void setProgressView(TextView progressView) {
		this.progressView = progressView;
	}
	
	public void setOnValueChangeListener(OnValueChangeListener vcl) {
		this.vcl = vcl;
	}
	
	/***
	 * Will not call OnValueChangeListener.onValueChange
	 */
	@Override 
	public void setProgress(int progress) {
		setOnSeekBarChangeListener(null);
		mCurrentValue = progress;
		
		if(mCurrentValue > getMax()) {
			mCurrentValue = getMax();
		}
		else if(mCurrentValue < mMinValue) {
			mCurrentValue = mMinValue;
		}

		try {
			if(progressView != null) {
				progressView.setText(String.valueOf(mCurrentValue) + mUnit);
			}
		} catch(Exception e){}
		
		super.setProgress(progress);
		setOnSeekBarChangeListener(this);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mCurrentValue = progress;
		
		if(mCurrentValue > seekBar.getMax()) {
			mCurrentValue = seekBar.getMax();
		}
		else if(mCurrentValue < mMinValue) {
			mCurrentValue = mMinValue;
		}

		try {
			if(progressView != null) {
				progressView.setText(String.valueOf(mCurrentValue) + mUnit);
			}
		} catch(Exception e){}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(vcl != null) {
			vcl.onValueChange(mCurrentValue);
		}
	}

}
