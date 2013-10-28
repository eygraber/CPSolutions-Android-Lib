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
			String[] proj = { MediaStore.Audio.Media.DATA };
			String[] whereArgs = { contentUri.getLastPathSegment() };
			cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Audio.Media._ID + "=?", whereArgs, null);
			if(cursor.getCount() > 0) {
				return true;
			}
		}
		catch(Exception e) {
			Logger.e("Error checking MediaStore to see if URI is a ringtone", e);
			return false;
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		Logger.w("Didn't find ringtone in MediaStore...checking RingtoneManager");
		
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
			String[] proj = { MediaStore.Audio.Media.DATA };
			String[] whereArgs = { contentUri.getLastPathSegment() };
			cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Audio.Media._ID + "=?", whereArgs, null);
			cursor.moveToFirst();
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
			return cursor.getString(column_index);
		}
		catch(Exception e) {
			Logger.e("Error getting audio path from URI", e);
			return null;
		}
		finally {
			if(cursor != null) {
				cursor.close();
			}
		}
    }
	
	public static String getDisplayNameFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		String name = null;
		try {
			String[] proj = { MediaStore.Audio.Media.DISPLAY_NAME };
			String[] whereArgs = { contentUri.getLastPathSegment() };
			cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Audio.Media._ID + "=?", whereArgs, null);
			if(cursor.moveToFirst()) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
				name = cursor.getString(column_index);
				int extensionPos = name.lastIndexOf(".");
				if(extensionPos != -1) {
					name = name.substring(0, extensionPos);
				}
				return name;
			}
		}
		catch(Exception e) {
			Logger.e("Error trying to get the display name from the MediaStore...trying the RingtoneManager");
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		Logger.w("Didn't find the display name in MediaStore...checking RingtoneManager");
		
		try {
			Ringtone ringtone = RingtoneManager.getRingtone(context, contentUri);
			if(ringtone == null) {
				Logger.w("Didn't find the display name in RingtoneManager...guess it doesn't exist");
				name = "Couldn't get ringtone display name";
			}
			else {
				name = ringtone.getTitle(context);
				if(name == null) {
					Logger.w("Didn't find the display name in RingtoneManager...guess it doesn't exist");
					name = "Couldn't get ringtone display name";
				}
			}
			return name;
		}
		catch(Exception e) {
			Logger.e("Error trying to get the display name from the RingtoneManager");
			return "Couldn't get ringtone display name";
		}
    }
}
