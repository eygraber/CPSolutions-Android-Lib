package com.customprogrammingsolutions.cpsolutions.utils;

import android.content.Context;
import android.media.AudioManager;

public class Utils {
	public static int getVolumeAdjustedForGlobal(int volume, Context caller) {
		final AudioManager audioManager = (AudioManager)caller.getSystemService(Context.AUDIO_SERVICE);
		float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		float v = ( volume / maxVolume );
		int adjustedVolume = (int) ( android.util.FloatMath.ceil(100 * v) );
		return adjustedVolume;
	}
	
	public static int getVolumeAdjustedForDevice(float volume, Context caller) {
		final AudioManager audioManager = (AudioManager)caller.getSystemService(Context.AUDIO_SERVICE);
		float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		float v = ( volume / 100 );
		int adjustedVolume = (int) ( android.util.FloatMath.ceil(maxVolume * v) );
		return adjustedVolume;
	}
}
