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
			cursor.close();
			RingtoneManager rm = new RingtoneManager(context);
			rm.setType(RingtoneManager.TYPE_ALL);
			cursor = rm.getCursor();
			if(!cursor.moveToFirst()) {
				return false;
			}
			do {
				try {
					if(contentUri.toString().equals(cursor.getString(RingtoneManager.URI_COLUMN_INDEX))) {
						return true;
					}
				}
				catch(Exception e) {
					Logger.e("Couldn't get ringtone info", e);
					continue;
				}
			} while(cursor.moveToNext());
			return false;
		}
		catch(Exception e) {
			Logger.e("Error checking if URI is a ringtone", e);
			return false;
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
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
			cursor.moveToFirst();
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
			name = cursor.getString(column_index);
			int extensionPos = name.lastIndexOf(".");
			if(extensionPos != -1) {
				name = name.substring(0, extensionPos);
			}
			return name;
		}
		catch(Exception e) {
			Logger.e("Can't get display name for audio from URI from the MediaStore...trying the RingtoneManager");

			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			Ringtone ringtone = RingtoneManager.getRingtone(context, contentUri);
			if(ringtone == null) {
				Logger.e("Couldn't get it from RingtoneManager either");
				name = "Couldn't get ringtone display name";
			}
			else {
				name = ringtone.getTitle(context);
				if(name == null) {
					Logger.e("Couldn't get it from RingtoneManager either");
					name = "Couldn't get ringtone display name";
				}
			}
			return name;
		}
		finally {
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
    }
}
