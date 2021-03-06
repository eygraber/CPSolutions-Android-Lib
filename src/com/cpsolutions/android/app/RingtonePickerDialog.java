package com.cpsolutions.android.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cpsolutions.R;
import com.cpsolutions.android.utils.Logger;
import com.cpsolutions.android.utils.RingtoneUtils;

public class RingtonePickerDialog extends ListFragment {
	private DialogFragment dialogContainer;
	private GetRingtonesThread worker;
	private RingtoneListAdapter adapter;
	private MediaPlayer mediaPlayer;
	private String selectedRingtone;
	private String defaultRingtone;
	
	private CompoundButton mCurrentButton;
	
	public interface OnRingtonePickedListener {
		public void ringtonePicked(String ringtoneUri);
	}
	
	private interface OnRingtonesReadyListener {
		public void onRingtonesReady(HashSet<RingtoneInfo> ringtones);
	}
	
	private class RingtoneInfo implements Comparable<RingtoneInfo> {
		String uri;
		String name;
		
		@Override
		public int compareTo(RingtoneInfo another) {
			if(uri.toLowerCase(Locale.ENGLISH).equals(selectedRingtone.toLowerCase(Locale.ENGLISH))) {
				return -1;
			}
			else if(another.uri.equalsIgnoreCase(selectedRingtone)) {
				return 1;
			}
			else if(uri.equalsIgnoreCase(defaultRingtone)) {
				return -1;
			}
			else if(another.uri.equalsIgnoreCase(defaultRingtone)) {
				return 1;
			}
			else {
				return name.compareToIgnoreCase(another.name);
			}
		}
		
		@Override
		public boolean equals(Object another) {
			return uri.equals(((RingtoneInfo)another).uri);
		}
		
		@Override
		public int hashCode() {
			return uri.hashCode();
		}
	}
	
	private class GetRingtonesThread extends Thread {
		private OnRingtonesReadyListener listener;
		private HashSet<RingtoneInfo> ringtones;
		
		public GetRingtonesThread(OnRingtonesReadyListener listener) {
			this.listener = listener;
		}
		
		private void getAudioMedia(Uri internalOrExternal) {
			String[] projec = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME};
			Cursor c = getActivity().getContentResolver().query(internalOrExternal, projec, null, null, null);
			if(c != null && !c.moveToFirst()) {
				if(!c.isClosed()) {
					c.close();
				}
				return;
			}
			if(c == null) {
				return;
			}
			do {
				try {
					int uriIndex = c.getColumnIndex(MediaStore.Audio.Media.DATA);
					int idIndex = c.getColumnIndex(MediaStore.Audio.Media._ID);
					int nameIndex = c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
					if(uriIndex != -1 && idIndex != -1 && nameIndex != -1) {
						RingtoneInfo ringtoneInfo = new RingtoneInfo();
						String uri = c.getString(uriIndex);
						if(!uri.startsWith("content://")) {
							uri = MediaStore.Audio.Media.getContentUriForPath(uri).toString();
							if(!uri.startsWith("content://")) {
								continue;
							}
						}
						uri += "/" + c.getString(idIndex);
						String name = c.getString(nameIndex);
						int extensionPos = name.lastIndexOf(".");
						if(extensionPos != -1) {
							name = name.substring(0, extensionPos);
						}
						ringtoneInfo.uri = uri;
						ringtoneInfo.name = name;
						ringtones.add(ringtoneInfo);
					}
				}
				catch(Exception e) {
					Logger.e("Got an error while getting audio media", e);
				}
			} while(c.moveToNext());
			c.close();
		}
		
		/*private void getRingtoneMedia() {
			RingtoneManager rm = new RingtoneManager(getActivity());
			rm.setType(RingtoneManager.TYPE_ALL);
			Cursor c = rm.getCursor();
			if(!c.moveToFirst()) {
				return;
			}
			
			do {
				int pos = c.getPosition();
				if(pos == -1) {
					continue;
				}
				RingtoneInfo ringtoneInfo = new RingtoneInfo();
				try {
					String uri = c.getString(RingtoneManager.URI_COLUMN_INDEX);
					if(!uri.startsWith("content://")) {
						continue;
					}
					else {
						uri += "/" + c.getString(RingtoneManager.ID_COLUMN_INDEX);
					}
					ringtoneInfo.uri = uri;
					ringtoneInfo.name = c.getString(RingtoneManager.TITLE_COLUMN_INDEX);
					if(ringtoneInfo.name.startsWith("Tza")) {
						Logger.e(ringtoneInfo.uri + " " + ringtoneInfo.name);
					}
				}
				catch(Exception e) {
					Logger.e("Couldn't get ringtone info", e);
					continue;
				}
				ringtones.add(ringtoneInfo);
			} while(c.moveToNext());
		}*/
		
		@Override
		public void run() {
			ringtones = new HashSet<RingtoneInfo>();
			
			try {
				defaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_ALL).toString();
			}
			catch(Exception e) {
				Logger.e("Device probably doesn't have a default ringtone...just set default to blank", e);
				defaultRingtone = "";
			}
			
			try {
				getAudioMedia(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
				sleep(100);
				
				getAudioMedia(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
				sleep(100);
			
				//getRingtoneMedia();
				//sleep(100);
				
				if(listener != null) {
					listener.onRingtonesReady(ringtones);
				}
			}
			catch(InterruptedException e) {
				Logger.e("Thread was interrupted", e);
				if(listener != null) {
					listener.onRingtonesReady(null);
				}
			}
		}
	};
	
	public RingtonePickerDialog() {}
	
	public static void show(FragmentManager fm, String tag, String selectedRingtone, OnRingtonePickedListener listener) {
		RingtonePickerContainerDialogFragment d = new RingtonePickerContainerDialogFragment();
		d.selectedRingtone = selectedRingtone;
		d.listener = listener;
		d.show(fm, tag);		
	}
	
	public static class RingtonePickerContainerDialogFragment extends DialogFragment {
		private String selectedRingtone;
		private OnRingtonePickedListener listener;
		private TextWatcher textChangeListener;
		
		public RingtonePickerContainerDialogFragment() {}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.ringtone_picker, container, false);
			
			getDialog().setTitle(getString(R.string.ringtone_picker_title));
			getDialog().setCancelable(false);
			setCancelable(false);
			
			final RingtonePickerDialog ringtonePicker = new RingtonePickerDialog();
			ringtonePicker.dialogContainer = this;
			ringtonePicker.selectedRingtone = selectedRingtone;
			getChildFragmentManager().beginTransaction().add(R.id.ringtone_picker_container, ringtonePicker).commit();
			
			v.findViewById(R.id.ringtone_picker_cancel).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			
			v.findViewById(R.id.ringtone_picker_ok).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(listener != null) {
						listener.ringtonePicked(ringtonePicker.selectedRingtone);
					}
					dismiss();
				}
			});
			
			textChangeListener = new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if(ringtonePicker == null || ringtonePicker.adapter == null) {
						return;
					}
					ringtonePicker.adapter.getFilter().filter(s);
				}

				@Override
				public void afterTextChanged(Editable arg0) {}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				
			};
			
			((EditText)v.findViewById(R.id.ringtone_picker_filter)).addTextChangedListener(textChangeListener);
			
			return v;
		}
		
		@Override
		public void onDestroyView() {
			try {
				((EditText)getView().findViewById(R.id.ringtone_picker_filter)).removeTextChangedListener(textChangeListener);
			}
			catch(Exception e) {
				Logger.e("Error trying to remove the text change listener from the ringtone filter EditText", e);
			}
			super.onDestroyView();
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		worker = new GetRingtonesThread(new OnRingtonesReadyListener() {
			@Override
			public void onRingtonesReady(HashSet<RingtoneInfo> ringtones) {
				if(ringtones == null) {
					getListView().post(new Runnable() {
						@Override
						public void run() {
							if(getActivity() != null) {
								Toast.makeText(getActivity(), "There was an error getting the ringtones. Please try again.", Toast.LENGTH_LONG).show();
							}
						}
					});
					dialogContainer.dismiss();
					return;
				}
				
				adapter = new RingtoneListAdapter(ringtones);
				try {
					getListView().post(new Runnable() {
						@Override
						public void run() {
							setEmptyText("There is no media that can be used as a ringtone!");
							setListAdapter(adapter);
						}
					});
				}
				catch(Exception e) {
					Logger.e("Tried to get the list view before it was instantiated", e);
				}
			}
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Logger.i("Starting the worker thread");
		worker.start();
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		if(adapter.getItem(position).uri == null) {
			Toast.makeText(getActivity(), "Error playing ringtone sample...", Toast.LENGTH_LONG).show();
			return;
		}
		
		adapter.notifyDataSetChanged();
		
		selectedRingtone = adapter.getItem(position).uri;
		
		Uri uri = Uri.parse(adapter.getItem(position).uri);
		
		if(mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		}
		else {
			mediaPlayer.setOnCompletionListener(null);
			mediaPlayer.setOnPreparedListener(null);
			mediaPlayer.reset();
		}
		
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
				mp = null;
				mediaPlayer = null;
			}
		});
		
		mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Toast.makeText(getActivity(), "Error playing ringtone sample...", Toast.LENGTH_LONG).show();
				Logger.e("Error playing ringtone. What = " + what + " extra = " + extra);
				return false;
			}
		});
		
		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				try {
					mp.start();
				}
				catch(Exception e) {}
			}
		});
		
		try {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mediaPlayer.setDataSource(getActivity().getApplicationContext(), uri);
			}
			catch(Exception e) {
				Logger.e("Setting data source first time didn't work", e);
				mediaPlayer.setDataSource(RingtoneUtils.getRealPathFromURI(getActivity(), uri));
			}
			mediaPlayer.prepareAsync();
		}
		catch(Exception e) {
			Toast.makeText(getActivity(), "Error playing ringtone sample...", Toast.LENGTH_LONG).show();
			Logger.e("Error setting media player data source", e);
		}
		
		CompoundButton buttonView = (CompoundButton) v.findViewById(R.id.ringtone_selected);
		if(mCurrentButton == null) {
			mCurrentButton = buttonView;
			mCurrentButton.setChecked(true);
		}
		else if(mCurrentButton != buttonView) {
			mCurrentButton.setChecked(false);
			mCurrentButton = buttonView;
			mCurrentButton.setChecked(true);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if(mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		
		if(worker != null) {
			if(worker.isAlive()) {
				worker.interrupt();
			}
		}
	}
	
	private class RingtoneListAdapter extends BaseAdapter implements Filterable {
		private ArrayList<RingtoneInfo> ringtoneArray;
		private ArrayList<RingtoneInfo> filteredRingtoneArray;
		private LayoutInflater inflater;
		
		@SuppressWarnings("unchecked")
		public RingtoneListAdapter(HashSet<RingtoneInfo> ringtoneSet) {
			ringtoneArray = new ArrayList<RingtoneInfo>(ringtoneSet);
			Collections.sort(ringtoneArray);
			filteredRingtoneArray = (ArrayList<RingtoneInfo>) ringtoneArray.clone();
			
			if(getActivity() != null) {
				inflater = getActivity().getLayoutInflater();
			}
		}
		
		@Override
		public int getCount() {
			return filteredRingtoneArray.size();
		}

		@Override
		public RingtoneInfo getItem(int position) {
			return filteredRingtoneArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			
			if(inflater == null) {
				if(getActivity() != null) {
					inflater = getActivity().getLayoutInflater();
				}
				else {
					return convertView;
				}
			}

	        if (convertView == null) {
	            convertView = inflater.inflate(R.layout.ringtone_picker_row, null);

	            holder = new ViewHolder();
	            holder.name = (TextView) convertView.findViewById(R.id.ringtone_name);
	            holder.selected = (RadioButton) convertView.findViewById(R.id.ringtone_selected);
	        } 
	        else {
	        	holder = (ViewHolder) convertView.getTag();
	        }
	        
	        String uri = getItem(position).uri;
	        String name = getItem(position).name;
	        if(uri.equals(defaultRingtone) && !name.contains("Default")) {
	        	name = "Default ringtone (" + name + ")";
	        	getItem(position).name = name;
	        }
	        
	        if(uri.equals(selectedRingtone)) {
	        	holder.selected.setChecked(true);
	        	if(mCurrentButton == null) {
					mCurrentButton = holder.selected;
				}
	        	else if(mCurrentButton != holder.selected) {
					mCurrentButton.setChecked(false);
					mCurrentButton = holder.selected;
				}
	        }
	        else {
	        	holder.selected.setChecked(false);
	        }
	        
	        holder.name.setText(name);
	        convertView.setTag(holder);
	        
	        return convertView;
		}
		
		@Override
		public Filter getFilter() {
			return new Filter() {
				@SuppressWarnings("unchecked")
				@Override
				protected FilterResults performFiltering(CharSequence filterString) {
					FilterResults results = new FilterResults();
					
					if(filterString == null || filterString.length() == 0) {
						Logger.i("Filtering ringtones with null or empty");
						//there shouldn't be any reason why we need to use FilterResults
						filteredRingtoneArray = (ArrayList<RingtoneInfo>) ringtoneArray.clone();
					}
					else {
						filteredRingtoneArray = new ArrayList<RingtoneInfo>();
						for(RingtoneInfo ringtone : ringtoneArray) {
							if(ringtone.name.toLowerCase(Locale.ENGLISH).startsWith(filterString.toString().toLowerCase(Locale.ENGLISH))) {
								filteredRingtoneArray.add(ringtone);
							}
						}
						Logger.i("Filtering ringtones using " + filterString + " gives us " + filteredRingtoneArray.size() + " results");
					}
					
					return results;
				}
				
				@Override
				protected void publishResults(CharSequence filterString, FilterResults filterResults) {
					notifyDataSetChanged();
				}
			};
		}
	}
	
	private static class ViewHolder {
    	TextView name;
    	RadioButton selected;
    }
}
