package com.cpsolutions.android.net;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import android.app.Notification;
import android.app.Service;
import android.os.Environment;

import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadCanceledListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadConnectionResetListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadConnectionRetryListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadErrorListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadFinishedListener;
import com.cpsolutions.net.BackgroundHttpDownloaderCallbacks.DownloadProgressListener;
import com.cpsolutions.net.InitHttpRequest;
import com.cpsolutions.net.NameValuePair;
import com.cpsolutions.utils.DateTimeUtils;
import com.cpsolutions.utils.FileUtils;

public class ServiceFileDownloaderGet extends AbsServiceFileDownloaderGet {
	Notification notification;
	int notificationId;
	boolean isNotificationForeground = false;
	
	protected int CHUNK_SIZE = 32768;
	
	/**
	 * Listener used to handle download callbacks.
	 */
	private DownloadProgressListener downloadProgressListener = null;
	
	/**
	 * Interface used to handle callbacks to set the GET's headers.
	 */
	private InitHttpRequest initHttpRequest = null;
	
	/**
	 * Interface used to handle the onError callback.
	 */
	private DownloadErrorListener errorListener = null;
	
	/**
	 * Interface used to handle the onCancel callback.
	 */
	private DownloadCanceledListener canceledListener = null;
	
	/**
	 * Interface used to handle the onFinish callback.
	 */
	private DownloadFinishedListener finishedListener = null;
	
	/**
	 * Interface used to handle the onRetry callback.
	 */
	private DownloadConnectionRetryListener retryListener = null;
	
	/**
	 * Interface used to handle the onReset callback.
	 */
	private DownloadConnectionResetListener resetListener = null;

	public ServiceFileDownloaderGet(String url, int notificationId, Service service) throws MalformedURLException {
		super(url, service);
		notification = new Notification();
		this.notificationId = notificationId;
	}
	
	public Notification getNotification() {
		return notification;
	}
	
	public int getNotificationId() {
		return notificationId;
	}
	
	public boolean getIsNotificationForeground() {
		return isNotificationForeground;
	}
	
	public void setIsNotificationForeground(boolean isNotificationForeground) {
		this.isNotificationForeground = isNotificationForeground;
	}
	
	/**
	 * Register callbacks to be invoked when there is download information.
	 * 
	 * @param downloadProgressListener the callback that will run
	 */
	public void setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
		this.downloadProgressListener = downloadProgressListener;
	}
	
	/**
	 * Register callbacks to be invoked when this GET's headers need to be initialized.
	 * 
	 * @param initHttpRequest the callback that will run
	 */
	public void setInitHttpRequest(InitHttpRequest initHttpRequest) {
		this.initHttpRequest = initHttpRequest;
	}
	
	/**
	 * Register callbacks to be invoked if there is an error with the download.
	 * 
	 * @param errorListener the callback that will run
	 */
	public void setDownloadErrorListener(DownloadErrorListener errorListener) {
		this.errorListener = errorListener;
	}
	
	/**
	 * Register callbacks to be invoked if the download is canceled.
	 * 
	 * @param canceledListener the callback that will run
	 */
	public void setDownloadCanceledListener(DownloadCanceledListener canceledListener) {
		this.canceledListener = canceledListener;
	}
	
	/**
	 * Register callbacks to be invoked when the download finishes.
	 * 
	 * @param errorListener the callback that will run
	 */
	public void setDownloadFinishedListener(DownloadFinishedListener finishedListener) {
		this.finishedListener = finishedListener;
	}
	
	/**
	 * Register callbacks to be invoked if the download is reset.
	 * 
	 * @param errorListener the callback that will run
	 */
	public void setDownloadConnectionRetryListener(DownloadConnectionRetryListener retryListener) {
		this.retryListener = retryListener;
	}
	
	/**
	 * Register callbacks to be invoked if the download is reset.
	 * 
	 * @param errorListener the callback that will run
	 */
	public void setDownloadConnectionResetListener(DownloadConnectionResetListener resetListener) {
		this.resetListener = resetListener;
	}
	
	@Override
	protected void onCancel() {
		if(canceledListener != null) {
			canceledListener.onCancel(this);
		}
	}

	@Override
	protected void onError(String errorMessage) {
		if(errorListener != null) {
			errorListener.onError(this, errorMessage);
		}
	}

	@Override
	protected void onFinish() {
		if(finishedListener != null) {
			finishedListener.onFinished(this);
		}
	}

	@Override
	protected void onReset() {
		if(resetListener != null) {
			resetListener.onReset(this);
		}
	}

	@Override
	protected void onRetryConnection(long msLeftToTimeout) {
		if(retryListener != null) {
			retryListener.onRetryConnection(this, msLeftToTimeout);
		}
	}
	
	/**
	 * Called when the download begins. If a DownloadProgressListener was not set, it will attempt to
	 * create a {@link FileOutputStream} that stream to a file created in the root of the external storage.
	 * The filename will be pulled from the filename in the URL; if that is not possible the filename
	 * will be made from the host and path of the URL. If this still doesn't work, the filename will 
	 * be generated from the date and time.
	 * 
	 * @return an {@link OutputStream} that the data read from the HTTP response will be written to.
	 */
	@Override
	protected OutputStream onDownloadStart() {
		if(downloadProgressListener != null) { 
			OutputStream os = downloadProgressListener.onDownloadStart(this);
			if(os != null) {
				return os;
			}
		}
		String filename = getFilename();
		if(filename == null || filename.equals("")) {
			filename = getUrl().getHost().replaceAll("\\.", "-") + getUrl().getPath();
			filename = filename.replaceAll("\\\\|\\/","");
		}
		try {
			filename = FileUtils.getUniqueFilename(Environment.getExternalStorageDirectory() + filename);
		}
		catch(IllegalArgumentException e) {
			filename = DateTimeUtils.getDescriptiveTimeString(System.currentTimeMillis());
			filename = FileUtils.getUniqueFilename(Environment.getExternalStorageDirectory() + filename);
		}
		
		try {
			return new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@Override
	protected void onDownloadProgress(boolean isIndeterminate, int progress, int max, String display) {
		if(downloadProgressListener != null) { 
			downloadProgressListener.onDownloadProgress(this, isIndeterminate, progress, max, display);
		}
	}
	
	@Override
	protected void onDownloadComplete(OutputStream outstream) {
		if(downloadProgressListener != null) { 
			downloadProgressListener.onDownloadComplete(this, outstream);
		}
	}
	
	@Override
	protected void setRequestHeaders(List<NameValuePair> headers, String url) {
		if(initHttpRequest != null) {
			initHttpRequest.setRequestHeaders(headers, url);
		}
	}

}
