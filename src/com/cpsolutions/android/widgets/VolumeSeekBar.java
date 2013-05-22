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
import android.media.AudioManager;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VolumeSeekBar extends SeekBar implements OnSeekBarChangeListener {
	private int maxVolume = 0;
	private TextView progressView = null;
	
	public interface OnVolumeChangeListener{
		public void onVolumeChanged();
	}
	private OnVolumeChangeListener vcl = null;
	
	public VolumeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public VolumeSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs){
		setMax(100);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VolumeSeekBar);
		int streamType = 0;
		float currentStreamVolume = 50;
		
		if(!isInEditMode()){
			streamType = a.getInt(R.styleable.VolumeSeekBar_streamType, AudioManager.STREAM_MUSIC);
			switch(streamType){
				case 0:
					streamType = AudioManager.STREAM_ALARM;
					break;
				case 1:
					streamType = AudioManager.STREAM_DTMF;
					break;
				case 2:
					streamType = AudioManager.STREAM_MUSIC;
					break;
				case 3:
					streamType = AudioManager.STREAM_NOTIFICATION;
					break;
				case 4:
					streamType = AudioManager.STREAM_RING;
					break;
				case 5:
					streamType = AudioManager.STREAM_SYSTEM;
					break;
				case 6:
					streamType = AudioManager.STREAM_VOICE_CALL;
					break;
				default:
					streamType = AudioManager.STREAM_MUSIC;
			}
		
			final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			maxVolume = audioManager.getStreamMaxVolume(streamType);
			currentStreamVolume = (float)audioManager.getStreamVolume(streamType);
		}
		
		float v = (float) ( currentStreamVolume / 100);
		int startingValue = (int) ( android.util.FloatMath.ceil(maxVolume * v) );
		setProgress(startingValue);
		incrementProgressBy(1);
		setOnSeekBarChangeListener(this);
	}
	
	public void setProgressView(TextView progressView){
		this.progressView = progressView;
	}
	
	public void setProgressFromVolume(int volume){
		float v = (float) ( (float)volume / maxVolume);
		int progress = (int) ( android.util.FloatMath.ceil(100 * v) );
		setProgress(progress);
	}
	
	public int getVolume(){
		float v = (float) ( (float)getProgress() / 100);
		int vol = (int) ( android.util.FloatMath.ceil(maxVolume * v) );
		return vol;
	}
	
	public void setOnVolumeChangeListener(OnVolumeChangeListener vcl) {
		this.vcl = vcl;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
		int newValue = progress;
		
		if(newValue > seekBar.getMax())
			newValue = seekBar.getMax();
		else if(newValue < 0)
			newValue = 0;

		try{
			if(progressView != null)
				progressView.setText(String.valueOf(newValue) + "%");
		}catch(Exception e){}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(vcl != null) {
			vcl.onVolumeChanged();
		}
	}

}
