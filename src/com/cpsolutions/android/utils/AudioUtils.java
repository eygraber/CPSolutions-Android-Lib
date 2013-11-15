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
	public static int getWeightUsedOnStreamVolume(Context context, int streamType, double volume) {
		return getWeightUsedOnStreamVolume((AudioManager)context.getSystemService(Context.AUDIO_SERVICE), streamType, volume);
	}
	
	public static int getWeightUsedOnStreamVolume(AudioManager am, int streamType, double volume) {
		int maxVolume = am.getStreamMaxVolume(streamType);
		if(volume > maxVolume) {
			return maxVolume;
		}
		return (int) Math.ceil((volume / (double) maxVolume) * 100.00);
	}
	
	public static int getWeightedStreamVolume(Context context, int streamType, double weight) {
		return getWeightedStreamVolume((AudioManager)context.getSystemService(Context.AUDIO_SERVICE), streamType, weight);
	}
	
	public static int getWeightedStreamVolume(AudioManager am, int streamType, double weight) {
		int maxVolume = am.getStreamMaxVolume(streamType);
		if(weight > 100) {
			return maxVolume;
		}
		return (int) Math.ceil((double) maxVolume * (weight / 100.00));
	}
	
	public static float getMediaPlayerScaledVolume(int maxVolume, int nonScalarVolume) {
		if(maxVolume == 0) {
			return 0;
		}
		if(nonScalarVolume > maxVolume) {
			return 1;
		}
		float result = (float) nonScalarVolume / maxVolume;
		if(result > 1) {
			result = 1;
		}
		if(result < 0) {
			result = 0;
		}
		return result;
	}
}
