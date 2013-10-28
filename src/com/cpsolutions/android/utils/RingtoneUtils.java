package com.cpsolutions.android.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;

public class RingtoneUtils {
	public static boolean isRingtone(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			cursor = getCursorForMedia(context, contentUri, false);
			if(cursor != null && cursor.getCount() > 0) {
				Logger.i("Found the ringtone in MediaStore external");
				return true;
			}
		}
		catch(Exception e) {
			Logger.e("Error checking MediaStore external content to see if URI is a ringtone", e);
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		Logger.w("Didn't find ringtone in MediaStore external...checking MediaStore internal");
		
		try {
			cursor = getCursorForMedia(context, contentUri, true);
			if(cursor != null && cursor.getCount() > 0) {
				Logger.i("Found the ringtone in MediaStore internal");
				return true;
			}
		}
		catch(Exception e) {
			Logger.e("Error checking MediaStore internal content to see if URI is a ringtone", e);
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		Logger.w("Didn't find ringtone in MediaStore internal...checking RingtoneManager");
		
		try {
			RingtoneManager rm = new RingtoneManager(context);
			rm.setType(RingtoneManager.TYPE_ALL);
			cursor = rm.getCursor();
			if(!cursor.moveToFirst()) {
				return false;
			}
			do {
				try {
					if(contentUri.toString().equals(cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX))) {
						Logger.i("Found the ringtone in RingtoneManager");
						return true;
					}
				}
				catch(Exception e) {
					Logger.w("Couldn't get ringtone info - checking next ringtone if there is one", e);
					continue;
				}
			} while(cursor.moveToNext());
			Logger.w("Didn't find ringtone in RingtoneManager...guess it doesn't exist");
			return false;
		}
		catch(Exception e) {
			Logger.e("Error checking RingtoneManager if URI is a ringtone", e);
			return false;
		}
	}
	
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			cursor = getCursorForMedia(context, contentUri, false);
			if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				String audioPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				if(audioPath != null) {
					Logger.i("Got the audio data path from MediaStore external");
					return audioPath;
				}
			}
		}
		catch(Exception e) {
			Logger.e("Error getting the audio data path from MediaStore external", e);
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		Logger.w("Didn't get the audio path from MediaStore external...trying MediaStore interal");
		
		try {
			cursor = getCursorForMedia(context, contentUri, true);
			if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				String audioPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				if(audioPath != null) {
					Logger.i("Got the audio data path from MediaStore internal");
					return audioPath;
				}
			}
		}
		catch(Exception e) {
			Logger.e("Error getting the audio data path from MediaStore internal", e);
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		Logger.w("Didn't get the audio path from MediaStore internal...guess it doesn't exist");
		
		return null;
    }
	
	public static String getDisplayNameFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		String name = null;
		try {
			cursor = getCursorForMedia(context, contentUri, false);
			if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
				name = cursor.getString(column_index);
				int extensionPos = name.lastIndexOf(".");
				if(extensionPos != -1) {
					name = name.substring(0, extensionPos);
				}
				Logger.i("Got the display from MediaStore external");
				return name;
			}
		}
		catch(Exception e) {
			Logger.e("Error getting the display name from MediaStore external", e);
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		Logger.w("Didn't get the display name from MediaStore external...trying MediaStore internal");
		
		try {
			cursor = getCursorForMedia(context, contentUri, true);
			if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
				name = cursor.getString(column_index);
				int extensionPos = name.lastIndexOf(".");
				if(extensionPos != -1) {
					name = name.substring(0, extensionPos);
				}
				Logger.i("Got the display from MediaStore internal");
				return name;
			}
		}
		catch(Exception e) {
			Logger.e("Error getting the display name from MediaStore internal", e);
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		Logger.w("Didn't get the display name from MediaStore internal...trying RingtoneManager");
		
		try {
			Ringtone ringtone = RingtoneManager.getRingtone(context, contentUri);
			if(ringtone == null) {
				Logger.w("Didn't get the display name from RingtoneManager...guess it doesn't exist");
				name = "Couldn't get ringtone display name";
			}
			else {
				name = ringtone.getTitle(context);
				if(name == null) {
					Logger.w("Didn't get the display name from RingtoneManager...guess it doesn't exist");
					name = "Couldn't get ringtone display name";
				}
				else {
					Logger.i("Got the display from RingtoneManager");
				}
			}
			return name;
		}
		catch(Exception e) {
			Logger.e("Error getting the display name from RingtoneManager");
			return "Couldn't get ringtone display name";
		}
    }
	
	private static Cursor getCursorForMedia(Context context, Uri contentUri, boolean useInternalStore) throws Exception {
		String[] proj = { MediaStore.Audio.Media.DISPLAY_NAME };
		String[] whereArgs = { contentUri.getLastPathSegment() };
		Uri mediaStoreUri = (useInternalStore ? MediaStore.Audio.Media.INTERNAL_CONTENT_URI : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
		return context.getContentResolver().query(mediaStoreUri, proj, MediaStore.Audio.Media._ID + "=?", whereArgs, null);
	}
}
