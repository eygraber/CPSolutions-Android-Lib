package com.cpsolutions.android.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cpsolutions.android.net.ParcelableNameValuePair;
import com.cpsolutions.android.net.ServiceFileDownloaderGet;
import com.cpsolutions.net.BackgroundHttpDownloader;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadConnectionResetListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadConnectionRetryListener;
import com.cpsolutions.net.InitHttpRequest;
import com.cpsolutions.net.NameValuePair;
import com.cpsolutions.utils.DateTimeUtils;
import com.cpsolutions.utils.FileUtils;
import com.cpsolutions.R;

public class DefaultFileDownloaderGetService extends AbsFileDownloaderService { 
	public static String CREATE_NOTIFICATION_INTENT_EXTRA = "com.cpsolution.android.app.DefaultFileDownloaderGetService.CREATE_NOTIFICATION_INTENT";
	public static String UPDATE_NOTIFICATION_INTENT_EXTRA = "com.cpsolution.android.app.DefaultFileDownloaderGetService.UPDATE_NOTIFICATION_INTENT";
	public static String PAUSE_NOTIFICATION_INTENT_EXTRA = "com.cpsolution.android.app.DefaultFileDownloaderGetService.PAUSE_NOTIFICATION_INTENT";
	public static String FINISH_NOTIFICATION_INTENT_EXTRA = "com.cpsolution.android.app.DefaultFileDownloaderGetService.FINISH_NOTIFICATION_INTENT";
	public static String ERROR_NOTIFICATION_INTENT_EXTRA = "com.cpsolution.android.app.DefaultFileDownloaderGetService.ERROR_NOTIFICATION_INTENT";
	public static String NO_ACTION_EXTRA = "com.cpsolution.android.app.DefaultFileDownloaderGetService.NO_ACTION";
	
	private HashMap<String, Intent> createNotificationIntentsMap = null;
	private HashMap<String, Intent> updateNotificationIntentsMap = null;
	private HashMap<String, Intent> pauseNotificationIntentsMap = null;
	private HashMap<String, Intent> finishNotificationIntentsMap = null;
	private HashMap<String, Intent> errorNotificationIntentsMap = null;
	
	private HashMap<String, ServiceFileDownloaderGet> backgroundDownloaderMap;
	private HashMap<String, String> backgroundDownloaderFileMap;
	private HashMap<String, String> backgroundDownloaderRootDirectoryMap;
	private HashMap<String, Boolean> connectionWasInterruptedMap;
	private HashMap<String, List<ParcelableNameValuePair>> backgroundDownloaderHeadersMap = null;
	private SparseBooleanArray notificationIds;	
	
	protected final String TAG = "DefaultFileDownloaderGetService";
	
	public interface OnResetListener {
		public void onReset(BackgroundHttpDownloader downloader);
	}
	private OnResetListener resetListener = null;
	
	public void setOnResetListener(OnResetListener resetListener) {
		this.resetListener = resetListener;
	}
	
	public interface OnStartCommandListener {
		public void onStartCommand(Intent i);
	}
	private OnStartCommandListener startCommandListener = null;
	
	public void setOnStartCommandListener(OnStartCommandListener startCommandListener) {
		this.startCommandListener = startCommandListener;
	}
	
	protected final class DownloadCallbacks extends AbsDownloadCallbacks implements DownloadConnectionResetListener, DownloadConnectionRetryListener, InitHttpRequest {
		public DownloadCallbacks() {}
		
		private int downloadNotificationIconLevel = 0;
		
		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadProgressListener#onDownloadStart(com.cpsolutions.net.BackgroundHttpDownloader)
		 */
		@Override
		public OutputStream onDownloadStart(BackgroundHttpDownloader downloader) {
			ServiceFileDownloaderGet backgroundDownloader = backgroundDownloaderMap.get(downloader.getUrlString());
			RemoteViews contentView = new RemoteViews(backgroundDownloader.getService().getPackageName(), R.layout.download_notification_layout);
			
			String filename = getFilename(downloader);
			int filenameIndex = filename.lastIndexOf("/");
			String displayFilename = "";
			
			if(filenameIndex < 0 || filenameIndex == filename.length()) {
				displayFilename = filename;
			}
			else {
				displayFilename = filename.substring(filenameIndex + 1);
			}
			
			contentView.setTextViewText(R.id.notification_title, displayFilename);
			contentView.setTextViewText(R.id.notification_text, "Contacting server...");
			contentView.setProgressBar(R.id.progress_bar, 0, 0, true);
			
			Intent i = createNotificationIntentsMap.get(downloader.getUrlString());
			if(i == null) {
				i = new Intent(DefaultFileDownloaderGetService.this, DefaultFileDownloaderGetService.class);
				i.putExtra(NO_ACTION_EXTRA, true);
			}
			PendingIntent contentIntent = PendingIntent.getActivity(backgroundDownloader.getService(), backgroundDownloader.getNotificationId(), i, 0);
			
			Notification notification = backgroundDownloader.getNotification();
			notification.icon = R.drawable.download_notification_icon_levels;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			
			createNotification(backgroundDownloader.getNotificationId(), notification);
			
			FileOutputStream fs;
			try {
				fs = new FileOutputStream(new File(filename), connectionWasInterruptedMap.get(downloader.getUrlString()));
			} 
			catch (FileNotFoundException e) {
				return null;
			}
			
			backgroundDownloaderFileMap.put(downloader.getUrlString(), filename);
			
			return fs;
		}
		
		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadProgressListener#onDownloadProgress(com.cpsolutions.net.BackgroundHttpDownloader, boolean, int, int, java.lang.String)
		 */
		@Override
		public void onDownloadProgress(BackgroundHttpDownloader downloader, boolean isIndeterminate, int progress, int max, String display) {
			ServiceFileDownloaderGet backgroundDownloader = backgroundDownloaderMap.get(downloader.getUrlString());
			RemoteViews contentView = new RemoteViews(backgroundDownloader.getService().getPackageName(), R.layout.download_notification_layout);
			
			String filename = backgroundDownloaderFileMap.get(downloader.getUrlString());
			int filenameIndex = filename.lastIndexOf("/");
			String displayFilename = "";
			
			if(filenameIndex < 0 || filenameIndex == filename.length()) {
				displayFilename = filename;
			}
			else {
				displayFilename = filename.substring(filenameIndex + 1);
			}
			contentView.setTextViewText(R.id.notification_title, displayFilename);
			contentView.setTextViewText(R.id.notification_text, display);
			if(isIndeterminate) {
				contentView.setTextViewText(R.id.notification_percent_done, "N/A");
				contentView.setProgressBar(R.id.progress_bar, 0, 0, isIndeterminate);
			}
			else {
				contentView.setTextViewText(R.id.notification_percent_done, progress + "%");
				contentView.setProgressBar(R.id.progress_bar, 100, progress, isIndeterminate);
			}
			
			if(downloadNotificationIconLevel == 5) {
				downloadNotificationIconLevel = 0;
			}
			contentView.setImageViewResource(R.id.notification_icon, R.drawable.download_notification_icon_levels);
			contentView.setInt(R.id.notification_icon, "setImageLevel", downloadNotificationIconLevel);
			
			Intent i = updateNotificationIntentsMap.get(downloader.getUrlString());
			if(i == null) {
				i = new Intent(DefaultFileDownloaderGetService.this, DefaultFileDownloaderGetService.class);
		        i.putExtra(CANCEL_DOWNLOAD_EXTRA, true);
		        i.putExtra(URL_TO_DOWNLOAD_EXTRA, backgroundDownloader.getUrlString());
			}
	        
	        PendingIntent contentIntentCancel = PendingIntent.getService(backgroundDownloader.getService(), backgroundDownloader.getNotificationId(), i, PendingIntent.FLAG_UPDATE_CURRENT);
			
			Notification notification = backgroundDownloader.getNotification();
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				notification.contentIntent = contentIntentCancel;
			}
	        else {
	        	contentView.setOnClickPendingIntent(R.id.cancel_button, contentIntentCancel);
	        }
			notification.contentView = contentView;
			notification.iconLevel = downloadNotificationIconLevel++;
			
			boolean becameForeground = updateNotification(backgroundDownloader.getUrlString(), backgroundDownloader.getNotificationId(), backgroundDownloader.getNotification());
			backgroundDownloader.setIsNotificationForeground(becameForeground);
		}

		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadProgressListener#onDownloadComplete(com.cpsolutions.net.BackgroundHttpDownloader, java.io.OutputStream)
		 */
		@Override
		public void onDownloadComplete(BackgroundHttpDownloader downloader, OutputStream outstream) {
			ServiceFileDownloaderGet backgroundDownloader = backgroundDownloaderMap.get(downloader.getUrlString());
			closeNotification(backgroundDownloader, false);
			
			NotificationManager nm = (NotificationManager)backgroundDownloader.getService().getSystemService(Context.NOTIFICATION_SERVICE);
			
			RemoteViews contentView = new RemoteViews(backgroundDownloader.getService().getPackageName(), R.layout.download_done_notification_layout);
			contentView.setImageViewResource(R.id.notification_icon, R.drawable.download_notification_successful);
			contentView.setTextViewText(R.id.notification_title, "Download Completed");
			contentView.setTextViewText(R.id.notification_text, downloader.getUrlString());	
			
			Intent i = finishNotificationIntentsMap.get(downloader.getUrlString());
			if(i == null) {
				i = new Intent(DefaultFileDownloaderGetService.this, DefaultFileDownloaderGetService.class);
				i.putExtra(NO_ACTION_EXTRA, true);
			}
			PendingIntent contentIntent = PendingIntent.getActivity(backgroundDownloader.getService(), 0, i, 0);		
			
			Notification notification = backgroundDownloader.getNotification();
			notification.icon = R.drawable.download_notification_successful;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			
			nm.notify(backgroundDownloader.getNotificationId(), notification);
			
			//cleanUpAfterDownload(downloader.getUrlString(), false);
		}		

		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadErrorListener#onError(com.cpsolutions.net.BackgroundHttpDownloader, java.lang.String)
		 */
		@Override
		public void onError(BackgroundHttpDownloader downloader, String errorMessage) {
			ServiceFileDownloaderGet backgroundDownloader = backgroundDownloaderMap.get(downloader.getUrlString());
			closeNotification(backgroundDownloader, true);
			
			NotificationManager nm = (NotificationManager)backgroundDownloader.getService().getSystemService(Context.NOTIFICATION_SERVICE);
			
			RemoteViews contentView = new RemoteViews(backgroundDownloader.getService().getPackageName(), R.layout.download_done_notification_layout);
			contentView.setImageViewResource(R.id.notification_icon, R.drawable.download_notification_error);
			contentView.setTextViewText(R.id.notification_title, "Download Error");
			if(errorMessage != null && errorMessage.compareTo("") != 0) {
				contentView.setTextViewText(R.id.notification_text, errorMessage);
			}
			else {
				contentView.setTextViewText(R.id.notification_text, "There was an error downloading " + downloader.getUrlString() + " from the server!");
			}	
			
			Intent i = errorNotificationIntentsMap.get(downloader.getUrlString());
			if(i == null) {
				i = new Intent(DefaultFileDownloaderGetService.this, DefaultFileDownloaderGetService.class);
				i.putExtra(NO_ACTION_EXTRA, true);
			}
			PendingIntent contentIntent = PendingIntent.getActivity(backgroundDownloader.getService(), 0, i, 0);		
			
			Notification notification = backgroundDownloader.getNotification();
			notification.icon = R.drawable.download_notification_error;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			
			nm.notify(backgroundDownloader.getNotificationId(), notification);
			
			//cleanUpAfterDownload(downloader.getUrlString(), true);
		}

		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadCanceledListener#onCancel(com.cpsolutions.net.BackgroundHttpDownloader)
		 */
		@Override
		public void onCancel(BackgroundHttpDownloader downloader) {
			ServiceFileDownloaderGet backgroundDownloader = backgroundDownloaderMap.get(downloader.getUrlString());
			closeNotification(backgroundDownloader, true);
			
			NotificationManager nm = (NotificationManager)backgroundDownloader.getService().getSystemService(Context.NOTIFICATION_SERVICE);
			
			RemoteViews contentView = new RemoteViews(backgroundDownloader.getService().getPackageName(), R.layout.download_done_notification_layout);
			contentView.setImageViewResource(R.id.notification_icon, R.drawable.download_notification_error);
			contentView.setTextViewText(R.id.notification_title, "Download Canceled");
			contentView.setTextViewText(R.id.notification_text, "The download " + downloader.getUrlString() + " was canceled!");
			
			Intent i = errorNotificationIntentsMap.get(downloader.getUrlString());
			if(i == null) {
				i = new Intent(DefaultFileDownloaderGetService.this, DefaultFileDownloaderGetService.class);
				i.putExtra(NO_ACTION_EXTRA, true);
			}
			PendingIntent contentIntent = PendingIntent.getActivity(backgroundDownloader.getService(), 0, i, 0);		
			
			Notification notification = backgroundDownloader.getNotification();
			notification.icon = R.drawable.download_notification_error;
			notification.contentView = contentView;
			notification.contentIntent = contentIntent;
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			
			nm.notify(backgroundDownloader.getNotificationId(), notification);
			
			//cleanUpAfterDownload(downloader.getUrlString(), true);
		}

		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadFinishedListener#onFinished(com.cpsolutions.net.BackgroundHttpDownloader)
		 */
		@Override
		public void onFinished(BackgroundHttpDownloader downloader) {
			//cleanUpAfterDownload(downloader.getUrlString(), false);
		}

		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadConnectionRetryListener#onRetryConnection(com.cpsolutions.net.BackgroundHttpDownloader, long)
		 */
		@Override
		public void onRetryConnection(BackgroundHttpDownloader downloader, long msLeftToTimeout) {
			ServiceFileDownloaderGet backgroundDownloader = backgroundDownloaderMap.get(downloader.getUrlString());
			RemoteViews contentView = new RemoteViews(backgroundDownloader.getService().getPackageName(), R.layout.download_notification_layout);
			
			String filename = backgroundDownloaderFileMap.get(downloader.getUrlString());
			int filenameIndex = filename.lastIndexOf("/");
			String displayFilename = "";
			
			if(filenameIndex < 0 || filenameIndex == filename.length()) {
				displayFilename = filename;
			}
			else {
				displayFilename = filename.substring(filenameIndex + 1);
			}
			contentView.setTextViewText(R.id.notification_title, displayFilename);
			contentView.setTextViewText(R.id.notification_text, "Waiting " + DateTimeUtils.getDescriptiveTimeString(msLeftToTimeout) + " to reconnect");
			contentView.setProgressBar(R.id.progress_bar, 0, 0, true);

			Intent i = pauseNotificationIntentsMap.get(downloader.getUrlString());
			if(i == null) {
				i = new Intent(DefaultFileDownloaderGetService.this, DefaultFileDownloaderGetService.class);
		        i.putExtra(CANCEL_DOWNLOAD_EXTRA, true);
		        i.putExtra(URL_TO_DOWNLOAD_EXTRA, backgroundDownloader.getUrlString());
			}
	        PendingIntent contentIntentCancel = PendingIntent.getService(backgroundDownloader.getService(), backgroundDownloader.getNotificationId(), i, PendingIntent.FLAG_UPDATE_CURRENT);
			contentView.setOnClickPendingIntent(R.id.cancel_button, contentIntentCancel);
			
			Notification notification = backgroundDownloader.getNotification();
			notification.contentView = contentView;
			
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				notification.contentIntent = contentIntentCancel;
			}
			
			boolean becameForeground = updateNotification(backgroundDownloader.getUrlString(), backgroundDownloader.getNotificationId(), backgroundDownloader.getNotification());
			backgroundDownloader.setIsNotificationForeground(becameForeground);
			
			connectionWasInterruptedMap.put(downloader.getUrlString(), true);
		}

		/* (non-Javadoc)
		 * @see com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadConnectionResetListener#onReset(com.cpsolutions.net.BackgroundHttpDownloader)
		 */
		@Override
		public void onReset(BackgroundHttpDownloader downloader) {
			Log.e(TAG, "I really shouldn't be in here");
			connectionWasInterruptedMap.put(downloader.getUrlString(), false);
			
			if(resetListener != null) {
				resetListener.onReset(downloader);
			}
		}

		@Override
		public void setRequestHeaders(List<NameValuePair> headers, String url) {
			if(backgroundDownloaderHeadersMap == null) {
				return;
			}
			List<ParcelableNameValuePair> headersToAdd = backgroundDownloaderHeadersMap.get(url);
			if(headersToAdd != null) {
				headers.addAll(headersToAdd);
			}
		}
	}
	
	private DownloadCallbacks callbacks;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		createNotificationIntentsMap = new HashMap<String, Intent>();
		updateNotificationIntentsMap = new HashMap<String, Intent>();
		pauseNotificationIntentsMap = new HashMap<String, Intent>();
		finishNotificationIntentsMap = new HashMap<String, Intent>();
		errorNotificationIntentsMap = new HashMap<String, Intent>();
		
		callbacks = new DownloadCallbacks();
		backgroundDownloaderMap = new HashMap<String, ServiceFileDownloaderGet>();
		backgroundDownloaderFileMap = new HashMap<String, String>();
		backgroundDownloaderRootDirectoryMap = new HashMap<String, String>();
		connectionWasInterruptedMap = new HashMap<String, Boolean>();
		notificationIds = new SparseBooleanArray();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		/*List<String> backgroundDownloaders = new ArrayList<String>(backgroundDownloaderMap.keySet());
		for(String url : backgroundDownloaders) {
			ServiceFileDownloaderGet downloader = backgroundDownloaderMap.get(url);
			if(downloader == null || downloader.isCancelled()) {
				continue;
			}
			downloader.cancel();
			backgroundDownloaderMap.remove(url);
		}*/
	}
	
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		if(intent == null) {
			Toast.makeText(this, "There was an error downloading the URL - 1", Toast.LENGTH_LONG).show();
			return START_NOT_STICKY;
		}
		
		if(intent.hasExtra(NO_ACTION_EXTRA)) {
			return START_NOT_STICKY;
		}
		
		boolean doCancel = false;
		
		if(intent.hasExtra(CANCEL_DOWNLOAD_EXTRA)) {
			doCancel = true;
		}
		
		if(!intent.hasExtra(URL_TO_DOWNLOAD_EXTRA)) {
			Toast.makeText(this, "There was an error downloading the URL - 2", Toast.LENGTH_LONG).show();
			return START_NOT_STICKY;
		}
		
		String url = intent.getStringExtra(URL_TO_DOWNLOAD_EXTRA);
		
		if(doCancel) {
			if(backgroundDownloaderMap.containsKey(url)) {
				backgroundDownloaderMap.get(url).cancel();
			}
			
			return START_NOT_STICKY;
		}
		
		if(backgroundDownloaderMap.containsKey(url)) {
			Toast.makeText(this, "This Url is already downloading!", Toast.LENGTH_LONG).show();
			return START_NOT_STICKY;
		}
		
		if(intent.hasExtra(HTTP_HEADERS_EXTRA)) {
			ArrayList<ParcelableNameValuePair> headers = intent.getParcelableArrayListExtra(HTTP_HEADERS_EXTRA); 
			backgroundDownloaderHeadersMap.put(url, headers);
		}
		
		if(intent.hasExtra(ROOT_DIRECTORY_TO_SAVE_IN_EXTRA)) {
			backgroundDownloaderRootDirectoryMap.put(url, intent.getStringExtra(ROOT_DIRECTORY_TO_SAVE_IN_EXTRA));
		}
		
		if(intent.hasExtra(CREATE_NOTIFICATION_INTENT_EXTRA)) {
			createNotificationIntentsMap.put(url, (Intent)intent.getParcelableExtra(CREATE_NOTIFICATION_INTENT_EXTRA));
		}
		if(intent.hasExtra(UPDATE_NOTIFICATION_INTENT_EXTRA)) {
			updateNotificationIntentsMap.put(url, (Intent)intent.getParcelableExtra(UPDATE_NOTIFICATION_INTENT_EXTRA));
		}
		if(intent.hasExtra(PAUSE_NOTIFICATION_INTENT_EXTRA)) {
			pauseNotificationIntentsMap.put(url, (Intent)intent.getParcelableExtra(PAUSE_NOTIFICATION_INTENT_EXTRA));
		}
		if(intent.hasExtra(FINISH_NOTIFICATION_INTENT_EXTRA)) {
			finishNotificationIntentsMap.put(url, (Intent)intent.getParcelableExtra(FINISH_NOTIFICATION_INTENT_EXTRA));
		}
		if(intent.hasExtra(ERROR_NOTIFICATION_INTENT_EXTRA)) {
			errorNotificationIntentsMap.put(url, (Intent)intent.getParcelableExtra(ERROR_NOTIFICATION_INTENT_EXTRA));
		}
		
		if(startCommandListener != null) {
			startCommandListener.onStartCommand(intent);
		}
		
		connectionWasInterruptedMap.put(url, false);
		
		Random r = new Random();
		int notificationId = 0;
		do {
			notificationId = r.nextInt();
		} while(notificationIds.get(notificationId));
		notificationIds.put(notificationId, true);
		
		ServiceFileDownloaderGet backgroundDownloader;
		try {
			backgroundDownloader = new ServiceFileDownloaderGet(url, notificationId, this);
		} catch (MalformedURLException e) {
			Toast.makeText(this, "There was an error downloading the URL - 3 " + e.getMessage(), Toast.LENGTH_LONG).show();
			return START_NOT_STICKY;
		}
		
		backgroundDownloader.setDownloadErrorListener(callbacks);
		backgroundDownloader.setDownloadCanceledListener(callbacks);
		backgroundDownloader.setDownloadFinishedListener(callbacks);
		backgroundDownloader.setDownloadConnectionResetListener(callbacks);
		backgroundDownloader.setDownloadConnectionRetryListener(callbacks);
		backgroundDownloader.setDownloadProgressListener(callbacks);
		backgroundDownloader.setInitHttpRequest(callbacks);
		
		backgroundDownloader.setRetriesAllowed(5);
		backgroundDownloader.setRetryTimeout(30000);		
		
		Toast.makeText(this, url + " will download now. Please check the notification bar for more info.", Toast.LENGTH_LONG).show();
		backgroundDownloaderMap.put(url, backgroundDownloader);
		backgroundDownloader.start();
		
		return START_NOT_STICKY;
	}

	@Override
	protected void createNotification(int notificationId, Notification notification) {
		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notificationId, notification);
	}
	
	@Override
	protected boolean updateNotification(String url, int notificationId, Notification notification) {
		boolean becameForeground = false;
		if(!isInForeground) {
			Log.i(TAG, "Update - I'm the new foreground " + url);
			isInForeground = true;
			startForeground(notificationId, notification);
			becameForeground = true;
		}

		createNotification(notificationId, notification);
		
		return becameForeground;
	}
	
	private void closeNotification(ServiceFileDownloaderGet backgroundDownloader, boolean isError) {
		cleanUpAfterDownload(backgroundDownloader.getUrlString(), isError);
		if(backgroundDownloaderMap.isEmpty()) {
			Log.i(TAG, "I'm the last one");
			backgroundDownloader.setIsNotificationForeground(false);
			isInForeground = false;
			stopForeground(true);
		}
		else {
			Boolean isForegroundNotification = backgroundDownloader.getIsNotificationForeground();
			if(isForegroundNotification) {
				Log.i(TAG, "Stop - I'm the foreground");
				backgroundDownloader.setIsNotificationForeground(false);
				isInForeground = false;
				stopForeground(true);
			}
		}
	}
	
	private void cleanUpAfterDownload(String url, boolean isError) {
		if(isError) {
			String filename = backgroundDownloaderFileMap.get(url);
			if(filename != null) {
				new File(filename).delete();
			}
		}
		backgroundDownloaderMap.remove(url);
		backgroundDownloaderFileMap.remove(url);
	}
	
	private String getFilename(BackgroundHttpDownloader downloader) {
		String rootDirectory = backgroundDownloaderRootDirectoryMap.get(downloader.getUrlString());
		if(rootDirectory == null) {
			rootDirectory = Environment.getExternalStorageDirectory().toString();
		}
		rootDirectory.trim();
		if(!rootDirectory.endsWith("/")) {
			rootDirectory += "/";
		}
		String filename = downloader.getFilename();
		if(filename == null || filename.equals("")) {
			filename = downloader.getUrl().getHost().replaceAll("\\.", "-") + downloader.getUrl().getPath().replaceAll("/", "-");
		}
		
		if(connectionWasInterruptedMap.get(downloader.getUrlString())) {
			filename = backgroundDownloaderFileMap.get(downloader.getUrlString());
			if(filename == null) {
				filename = "";
			}
			File f = new File(filename);
			if(!f.exists()) {
				int copyOfFileNum = 1;
				String numAppend = "";
				File f2 = new File(filename + copyOfFileNum);
				while(f2.exists()) {
					numAppend = Integer.toString(copyOfFileNum);
					copyOfFileNum++;
				}
				filename += numAppend;
			}
		}
		else {
			try {
				filename = FileUtils.getUniqueFilename(rootDirectory + filename);
			}
			catch(IllegalArgumentException e) {
				filename = FileUtils.getUniqueFilename(rootDirectory + DateTimeUtils.getDescriptiveTimeString(System.currentTimeMillis()));
			}
		}
		return filename;
	}

}
