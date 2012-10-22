package com.customprogrammingsolutions.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.customprogrammingsolutions.utils.DateTimeUtils;

public class FileDownloaderThread extends Thread {
	private String url;
	private int notificationId;
	private int readsBetweenUpdates;
	private Context caller;
	private String LOG_TAG = "DefaultFileDownloaderThread";
	
	private boolean doChunkedDownload = false;
	private boolean doNotChunkedDownload = false;
	private int chunkSize = 4096;
	private int retrySeconds = 30;
	
	private Notification notification;
	
	public interface NotificationCreator {
		public void createNotification(Context context, String url, String filename, int notificationId, Notification notification);
	}
	
	public interface NotificationUpdater {
		public void updateNotification(Context context, boolean isIndeterminate, int currentProgress, String defaultTitle, String defaultMessage, String url, String filename, int notificationId, Notification notification);
	}
	
	public interface NotificationPauser {
		public void pauseNotification(Context context, int howMuchTimeLeftToPause, String url, String filename, int notificationId, Notification notification);
	}
	
	public interface NotificationStopper {
		public void stopNotification(Context context, boolean isError, String errorMessage, String url, String filename, int notificationId, Notification notification);
	}
	
	public interface OnGotChunkListener {
		public void onGotChunk(String url, byte[] chunk);
	}
	
	public interface OnErrorListener {
		public void onError(Context caller, String url);
	}
	
	public interface OnDownloadCompletionListener {
		public void onDownloadComplete(String url, LinkedList<byte[]> result);
	}
	
	public interface OnFinishedListener {
		public void onFinished(Context caller, String url);
	}
	
	private NotificationCreator notificationCreator = null;
	private NotificationUpdater notificationUpdater = null;
	private NotificationPauser notificationPauser = null;
	private NotificationStopper notificationStopper = null;
	private OnGotChunkListener gotChunkListener = null;
	private OnErrorListener errorListener = null;
	private OnDownloadCompletionListener downloadCompletionListener = null;
	private OnFinishedListener finishedListener = null;
	
	private BufferedInputStream instream;
	
	public FileDownloaderThread(Context caller, String logTag, String url, int notificationId) {
		this(caller, logTag, url, notificationId, 1000);
	}
	
	public FileDownloaderThread(Context caller, String logTag, String url, int notificationId, int readsBetweenUpdates) {
		this.caller = caller;
		this.LOG_TAG = logTag;
		this.url = url;
		this.notificationId = notificationId;
		if(readsBetweenUpdates < 1) {
			readsBetweenUpdates = 1;
		}
		this.readsBetweenUpdates = readsBetweenUpdates;
		
		this.notification = new Notification();
		
		super.start();
	}
	
	@Override
	public void start() {
		//prevents user from calling start since we start it automatically in the constructor
		//make sure the call in the constructor isn't coming to here
	}
	
	public void setWaitTimeOnLostConnectionRetry(int seconds) {
		retrySeconds = seconds;
	}
	
	public void setNotificationCreator(NotificationCreator notificationCreator) {
		this.notificationCreator = notificationCreator;
	}
	
	public void setNotificationUpdater(NotificationUpdater notificationUpdater) {
		this.notificationUpdater = notificationUpdater;
	}

	public void setNotificationPauser(NotificationPauser notificationPauser) {
		this.notificationPauser = notificationPauser;
	}

	public void setNotificationStopper(NotificationStopper notificationStopper) {
		this.notificationStopper = notificationStopper;
	}
	
	public void setOnErrorListener(OnErrorListener errorListener) {
		this.errorListener = errorListener;
	}
	
	public void setOnFinishedListener(OnFinishedListener finishedListener) {
		this.finishedListener = finishedListener;
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		try {
			if(instream != null) {
				try {
					instream.close();
				}
				catch(Exception e) {}
				instream = null;
	        }
		} 
		catch (Exception e) {} // quietly close
		finally {
			
		}
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				if(doChunkedDownload || doNotChunkedDownload) {
					createNotification();
					if(doDownload(0, 0, false)) {
						finish(false);
						return;
					}
					else {
						finish(true);
						return;
					}
				}
				else {
					sleep(1000);
				}
			}
		}
		catch(InterruptedException e) {
			Log.e(LOG_TAG, "ErrorNOT - we were interrupted");
			finish(true);
			return;
		}
		catch(Exception e) {
			Log.e(LOG_TAG, "Error5", e);
			finish(true);
			return;
		}
	}
	
	private void finish(boolean isError) {
		if(isError) {
			if(isInterrupted()) {
				stopNotification(true, getFilenameFromUrl() + " download was canceled by user!");
				Thread.currentThread().interrupt();
			}
			else {
				stopNotification(true, getFilenameFromUrl() + " - There was an error connecting to the server!");
			}
			
			if(errorListener != null) {
				errorListener.onError(caller, url);
			}
			new File(Environment.getExternalStorageDirectory() + "/VidsUrl/" + getFilenameFromUrl()).delete();
		}
		else {
			stopNotification(false, "");
		}
		if(finishedListener != null) {
			finishedListener.onFinished(caller, url);
		}
	}
	
	public void startDownload(OnDownloadCompletionListener l) {
		if(doChunkedDownload || doNotChunkedDownload) {
			throw new IllegalStateException("This download was already started");
		}
		
		if(l == null) {
			throw new IllegalArgumentException("OnDownloadCompletionListener l must not be null");
		}
		
		downloadCompletionListener = l;
		doNotChunkedDownload = true;
	}
	
	public void startChunkedDownload(OnGotChunkListener l) {
		if(doChunkedDownload || doNotChunkedDownload) {
			throw new IllegalStateException("This download was already started");
		}
		
		if(l == null) {
			throw new IllegalArgumentException("OnGotChunkListener l must not be null");
		}
		
		gotChunkListener = l;
		doChunkedDownload = true;
	}
	
	public void startChunkedDownload(OnGotChunkListener l, int chunkSize) {
		if(doChunkedDownload || doNotChunkedDownload) {
			throw new IllegalStateException("This download was already started");
		}
		
		if(l == null) {
			throw new IllegalArgumentException("OnGotChunkListener l must not be null");
		}
		
		gotChunkListener = l;
		this.chunkSize = chunkSize;
		doChunkedDownload = true;
	}
	
	public void cancel() {
		this.interrupt();
	}
	
	private boolean doDownload(float offset, float contentLength, boolean isRetry) 
			throws InterruptedException, ClientProtocolException, IllegalStateException, IOException, FileNotFoundException, IndexOutOfBoundsException, NullPointerException, Exception {
		HttpClient httpClient = new DefaultHttpClient();
		URL realUrl = new URL(url);
		URI uri;
		String urlToUse = url;
		try {
			uri = new URI(realUrl.getProtocol(), realUrl.getUserInfo(), realUrl.getHost(), realUrl.getPort(), realUrl.getPath(), realUrl.getQuery(), realUrl.getRef());
			urlToUse = uri.toString();
		} 
		catch (URISyntaxException e) {
			Log.e(LOG_TAG, "Error1", e);
		}
		
		Log.i(LOG_TAG, (isRetry ? "Re-" : "") + "Downloading " + urlToUse);
		HttpGet httpGet = new HttpGet(urlToUse);
		if(offset > 0 && contentLength > offset) {
			long offsetL = (long) ( android.util.FloatMath.ceil(offset));
			long contentLengthL = (long) ( android.util.FloatMath.ceil(contentLength));
			Log.i(LOG_TAG, "making range request bytes=" + NetworkUtils.getPrettyByteCount(offsetL) + "-" + NetworkUtils.getPrettyByteCount(contentLengthL));
			httpGet.addHeader("Range", "bytes=" + offsetL + "-" + contentLengthL);
		}
		HttpResponse response;
		response = httpClient.execute(httpGet);
		
		sleep(250);
		
		for(int i = 0; response.getStatusLine() == null && i < 25; i++) {
			sleep(500);
		}
		
		int responseCode = response.getStatusLine().getStatusCode();
		
		//206 is a partial content response
		if(responseCode != 200 && responseCode != 206) {
			Log.e(LOG_TAG, "Error2 - bad response code " + responseCode);
			return false;
		}
		Log.i(LOG_TAG, "Received responseCode " + responseCode);
		
		HttpEntity entity = response.getEntity();
		
		if(entity == null) {
			Log.e(LOG_TAG, "Error 3 - response entity is null");
			return false;
		}
		
		instream = new BufferedInputStream (entity.getContent());
        if (instream == null) {
        	Log.e(LOG_TAG, "Error4 - response entity InputStream is null");
            return false;
        }
        
        sleep(250);
        
        float bytesRead = offset;
        if(contentLength <= 0) {
        	contentLength = entity.getContentLength();
	        if (contentLength <= 0) {
	        	contentLength = 16384;
	        }
        }
        
        Log.i(LOG_TAG, NetworkUtils.getPrettyByteCount(offset) + "/" + NetworkUtils.getPrettyByteCount(contentLength));
        
        sleep(250);
        
        if(doNotChunkedDownload) {
        	LinkedList<byte[]> chunks = new LinkedList<byte[]>();
    		byte data[] = new byte[chunkSize];
    		try {
    			while ((instream.read(data, 0, chunkSize)) != -1) {
    				chunks.add(data);
    			}
    		}
    		catch(IOException e) {
    			if(isInterrupted()) {
    				throw e;
    			}
    			else if(isRetry) {
            		throw e;
            	}
            	else {
            		if(NetworkUtils.hasNetworkConnection(caller)) {
            			throw e;
            		}
            		
            		Log.i(LOG_TAG, "Waiting to see if network connection comes back");
            		
            		int timeLeftToReconnect = retrySeconds;
            		while(!NetworkUtils.hasNetworkConnection(caller)) {
            			if(timeLeftToReconnect == 0) {
            				Log.i(LOG_TAG, "Waited " + DateTimeUtils.getDescriptiveTimeString(retrySeconds * 1000) + " to see if network connection would come back, but it didn't");
            				throw e;
            			}
            			pauseNotification(timeLeftToReconnect);
            			timeLeftToReconnect--;
            			sleep(1000);
            		}
            		return doDownload(0, contentLength, true);
            	}
    		}
    		try {instream.close();} catch(Exception e) {} //fail quietly
    		downloadCompletionListener.onDownloadComplete(url, chunks);
        }
        else if(doChunkedDownload) {
        	byte data[] = new byte[chunkSize];
            int count = 0;
            int updateNotification = 0;
            float prevBytesRead = bytesRead;
            float bytesPerSecondAverager = 0;
            try {
            	while ((count = instream.read(data, 0, chunkSize)) != -1) {
            		if(isInterrupted()) {
            			throw new InterruptedException();
            		}
            		gotChunkListener.onGotChunk(url, data);
                    bytesRead += count;
                    if(++updateNotification % readsBetweenUpdates == 0) {
                    	if(bytesRead < contentLength && contentLength > 0) {
                    		int howManyUpdates = updateNotification / readsBetweenUpdates;
                        	float v = bytesRead / contentLength;
                        	int percentage = (int) ( android.util.FloatMath.ceil(100 * v) );
                        	if(percentage > 100) {
                        		percentage = 100;
                        	}
                        	float bytesPerSecond = bytesRead - prevBytesRead;
                        	bytesPerSecondAverager += bytesPerSecond;
                        	float averagedBytesPerSecond = bytesPerSecondAverager / howManyUpdates;
                        	prevBytesRead = bytesRead;
                        	float lengthLeft = contentLength - bytesRead; 
                        	long timeLeft = (long) android.util.FloatMath.ceil((lengthLeft / averagedBytesPerSecond)) * 1000;
                        	String prettyTimeLeft = DateTimeUtils.getDescriptiveTimeString(timeLeft, true);
                        	String prettyBytesPerSecond = NetworkUtils.getPrettyByteCount(averagedBytesPerSecond);
                        	updateNotification(false, percentage, getFilenameFromUrl(), prettyTimeLeft + " " + prettyBytesPerSecond + "/s");
                    	}
                    	else {
                    		updateNotification(true, 0, getFilenameFromUrl(), "Downloading File From Server");
                    	}
                    	
                    }
                    //if this was a retry we set it to false once we know for sure a connection was successfully established
                    //this way, if we lose network connection again we could try to recover from that
                    if(isRetry) {
                    	isRetry = false;
                    }
                }
            }
            catch(IOException e) {
            	if(isInterrupted()) {
    				throw e;
    			}
            	else if(isRetry) {
            		throw e;
            	}
            	else {
            		if(NetworkUtils.hasNetworkConnection(caller)) {
            			throw e;
            		}
            		
            		Log.i(LOG_TAG, "Waiting to see if network connection comes back");
            		
            		int timeLeftToReconnect = retrySeconds;
            		while(!NetworkUtils.hasNetworkConnection(caller)) {
            			if(timeLeftToReconnect == 0) {
            				Log.i(LOG_TAG, "Waited " + DateTimeUtils.getDescriptiveTimeString(retrySeconds * 1000) + " to see if network connection would come back, but it didn't");
            				throw e;
            			}
            			pauseNotification(timeLeftToReconnect);
            			timeLeftToReconnect--;
            			sleep(1000);
            		}
            		return doDownload(bytesRead, contentLength, true);
            	}
            }
            finally {
            	try {instream.close();} catch(Exception e) {} //fail quietly
            }
        }
        
        return true;
	}
	
	public String getFilenameFromUrl() {
		return url.substring(url.lastIndexOf("/") + 1);
	}
	
	private void createNotification() {
		if(notificationCreator != null) {
			notificationCreator.createNotification(caller, url, getFilenameFromUrl(), notificationId, notification);
		}
	}
	
	private void updateNotification(boolean isIndeterminate, int currentProgress, String title, String msg) {
		if(notificationUpdater != null) {
			notificationUpdater.updateNotification(caller, isIndeterminate, currentProgress, title, msg, url, getFilenameFromUrl(), notificationId, notification);
		}
	}
	
	private void pauseNotification(int howMuchTimeLeftToPause) {
		if(notificationPauser != null) {
			notificationPauser.pauseNotification(caller, howMuchTimeLeftToPause, url, getFilenameFromUrl(), notificationId, notification);
		}
	}
	
	private void stopNotification(boolean isError, String errorMessage) {
		if(notificationStopper != null) {
			notificationStopper.stopNotification(caller, isError, errorMessage, url, getFilenameFromUrl(), notificationId, notification);
		}
	}
}
