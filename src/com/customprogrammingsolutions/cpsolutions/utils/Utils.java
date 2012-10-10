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
