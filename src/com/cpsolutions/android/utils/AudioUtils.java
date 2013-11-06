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

package com.cpsolutions.android.utils;

import android.content.Context;
import android.media.AudioManager;

public class AudioUtils {
	public static int getVolumeAdjustedForGlobal(int volume, Context caller) {
		final AudioManager audioManager = (AudioManager)caller.getSystemService(Context.AUDIO_SERVICE);
		float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		float v = ( volume / maxVolume );
		int adjustedVolume = (int) ( java.lang.Math.ceil(100 * v) );
		return adjustedVolume;
	}
	
	public static int getVolumeAdjustedForDevice(float volume, Context caller) {
		final AudioManager audioManager = (AudioManager)caller.getSystemService(Context.AUDIO_SERVICE);
		float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		float v = ( volume / 100 );
		int adjustedVolume = (int) ( java.lang.Math.ceil(maxVolume * v) );
		return adjustedVolume;
	}
	
	public static float getMediaPlayerScaledVolume(int maxVolume, int nonScalarVolume) {
		return (float) (1 - (Math.log(maxVolume - nonScalarVolume) / Math.log(maxVolume)));
	}
}
