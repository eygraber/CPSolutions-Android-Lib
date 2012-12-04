package com.cpsolutions.android.app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadCanceledListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadErrorListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadFinishedListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadProgressListener;

//subclasses should implement AbsDownloadCallbacks so that the callback methods aren't public
public abstract class AbsFileDownloaderService extends Service {
	public final static String URL_TO_DOWNLOAD_EXTRA = "com.cpsolutions.android.app.AbsFileDownloaderService.URL_TO_DOWNLOAD";
	public final static String CANCEL_DOWNLOAD_EXTRA = "com.cpsolutions.android.app.AbsFileDownloaderService.CANCEL_DOWNLOAD";
	public final static String HTTP_HEADERS_EXTRA = "com.cpsolutions.android.app.AbsFileDownloaderService.HTTP_HEADERS";
	public final static String HTTP_POST_PARAMETERS_EXTRA = "com.cpsolutions.android.app.AbsFileDownloaderService.HTTP_POST_PARAMETERS";
	public final static String ROOT_DIRECTORY_TO_SAVE_IN_EXTRA = "com.cpsolutions.android.app.AbsFileDownloaderService.ROOT_DIRECTORY_TO_SAVE_IN";
	//private static final String TAG = "AbsFileDownloaderService";

	protected boolean isInForeground = false;
	
	protected abstract class AbsDownloadCallbacks implements DownloadProgressListener, DownloadErrorListener, DownloadCanceledListener, DownloadFinishedListener {}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected abstract void createNotification(int notificationId, Notification notification);
	protected abstract boolean updateNotification(String url, int notificationId, Notification notification);
}
