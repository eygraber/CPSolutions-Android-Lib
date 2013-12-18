package com.cpsolutions.android.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.Date;

import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Logger extends IntentService {
	private static final String LOG_LEVEL_EXTRA = "log_level_extra";
	private static final String LOG_SOURCE_FILE_EXTRA = "log_source_file_extra";
	private static final String LOG_SOURCE_LINE_EXTRA = "log_source_line_extra";
	private static final String LOG_SOURCE_METHOD_EXTRA = "log_source_method_extra";
	private static final String LOG_MESSAGE_EXTRA = "log_message_extra";
	private static final String LOG_THROWABLE_EXTRA = "log_throwable_extra";
	
	private static String TAG = "Logger";
	private static boolean LOG_TO_ANDROID_DEFAULT = false;
	private static int LOG_MAX_SIZE = 1024 * 512;
	private static WeakReference<Context> startServiceContext;
	
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public Logger() {
		super("Logger Service");
	}	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent == null || !intent.hasExtra(LOG_MESSAGE_EXTRA)) {
			Log.e(TAG, "Logger.onHandleIntent() - no message");
			return;
		}
		
		String message = createFullMessage(intent);
		if(message == null) {
			return;
		}

		if(startServiceContext != null && startServiceContext.get() != null) {
			try {
				File log = new File(startServiceContext.get().getFilesDir(), "log");
				File afterCrash = new File(startServiceContext.get().getFilesDir(), "afterCrash");
				if(afterCrash.exists()) {
					afterCrash.delete();
					Log.e(TAG, "We got a crash");
				}
				if(log.exists()) {
					if(log.length() > LOG_MAX_SIZE) { //512KB
						log.delete();
					}
				}
				
				PrintWriter pw = new PrintWriter(startServiceContext.get().openFileOutput("log", Context.MODE_APPEND));
				pw.println(message);
		        pw.close();
	    	}
	    	catch(Exception e) {
	        	Log.e(TAG, "Got an error in Logger.onHandleIntent()", e);
	    	}
		}
		else {
			Log.e(TAG, "Couldn't get a context in Logger.onHandleIntent()");
		}
	}
	
	private String createFullMessage(Intent intent) {
		int level = intent.getIntExtra(LOG_LEVEL_EXTRA, -1);
		String sourceFile = intent.getStringExtra(LOG_SOURCE_FILE_EXTRA);
		String sourceLine = Integer.toString(intent.getIntExtra(LOG_SOURCE_LINE_EXTRA, -1));
		String sourceMethod = intent.getStringExtra(LOG_SOURCE_METHOD_EXTRA);
		String message = intent.getStringExtra(LOG_MESSAGE_EXTRA);
		Throwable t = null;
		if(intent.hasExtra(LOG_THROWABLE_EXTRA)) {
			t = (Throwable)intent.getSerializableExtra(LOG_THROWABLE_EXTRA);
		}
		if(t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			message += "\n" + sw.toString();
			pw.close();
		}
		
		try {
			JSONObject json = new JSONObject();
			json.put("LEVEL", printLogLevel(level))
				.put("SOURCE_FILE", sourceFile)
				.put("SOURCE_LINE", sourceLine)
				.put("SOURCE_METHOD", sourceMethod)
				.put("MESSAGE", message)
				.put("TIMESTAMP", new Date().getTime());
			return json.toString();
			
		}
		catch(Exception e) {
			Log.e(TAG, "Could not build the message in Logger.onHandleIntent()", e);
			return null;
		}
	}
	
	public static void init(Context context) {
		startServiceContext = new WeakReference<Context>(context);
	}
	
	public static void init(Context context, String tag) {
		startServiceContext = new WeakReference<Context>(context);
		TAG = tag;
	}
	
	public static void init(Context context, String tag, boolean logToAndroidDefault) {
		startServiceContext = new WeakReference<Context>(context);
		TAG = tag;
		LOG_TO_ANDROID_DEFAULT = logToAndroidDefault;
	}
	
	public static void init(Context context, String tag, int maxLogFileSizeInBytes) {
		startServiceContext = new WeakReference<Context>(context);
		TAG = tag;
		LOG_MAX_SIZE = maxLogFileSizeInBytes;
	}
	
	public static void init(Context context, String tag, boolean logToAndroidDefault, int maxLogFileSizeInBytes) {
		startServiceContext = new WeakReference<Context>(context);
		TAG = tag;
		LOG_TO_ANDROID_DEFAULT = logToAndroidDefault;
		LOG_MAX_SIZE = maxLogFileSizeInBytes;
	}
	
	private static void log(String message, Throwable t, int level, boolean logToAndroid) {
		if(logToAndroid) {
			logToAndroid(createAndroidMessage(message), t, level);
		}
		
		if(message != null && startServiceContext != null && startServiceContext.get() != null) {
			startServiceContext.get().startService(getLogServiceIntent(message, level, t));
		}
		else {
			Log.e(TAG, "Couldn't get a context in Logger.onHandleIntent()");
		}
	}
	
	private static Intent getLogServiceIntent(String message, int level, Throwable t) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[5];
		String[] classNames = ste.getClassName().split("\\.");
		String method = (classNames.length > 0 ? classNames[classNames.length - 1] : ste.getClassName()) + "." + 
				ste.getMethodName() + "()";
		
		Intent intent = new Intent(startServiceContext.get(), Logger.class);
		intent.putExtra(LOG_LEVEL_EXTRA, level);
		intent.putExtra(LOG_SOURCE_FILE_EXTRA, ste.getFileName());
		intent.putExtra(LOG_SOURCE_LINE_EXTRA, ste.getLineNumber());
		intent.putExtra(LOG_SOURCE_METHOD_EXTRA, method);
		intent.putExtra(LOG_MESSAGE_EXTRA, message);
		if(t != null) {
			intent.putExtra(LOG_THROWABLE_EXTRA, t);
		}
		
		return intent;
	}
	
	private static String createAndroidMessage(String message) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[5];
		String[] classNames = ste.getClassName().split("\\.");
		return ste.getFileName() + ":" + 
				ste.getLineNumber() + " - " + 
				(classNames.length > 0 ? classNames[classNames.length - 1] : ste.getClassName()) + "." + 
				ste.getMethodName() + "() - " + 
				message;
	}
	
	private static final class LogTask implements Runnable {
		private String message;
		private Throwable t;
		private int level;
		private StackTraceElement ste;
		private boolean logToAndroid;
		
		public LogTask(String message, Throwable t, int level, StackTraceElement ste, boolean logToAndroid) {
			this.message = message;
			this.t = t;
			this.level = level;
			this.ste = ste;
			this.logToAndroid = logToAndroid;
		}
		
		@Override
		public void run() {
			if(logToAndroid) {
				logToAndroid(createAndroidMessage(message), t, level);
			}
			
			String message = createMessage();
			if(message == null) {
				return;
			}

			if(startServiceContext != null && startServiceContext.get() != null) {
				try {
					File log = new File(startServiceContext.get().getFilesDir(), "log");
					File afterCrash = new File(startServiceContext.get().getFilesDir(), "afterCrash");
					if(afterCrash.exists()) {
						afterCrash.delete();
						Log.e(TAG, "We got a crash");
					}
					if(log.exists()) {
						if(log.length() > LOG_MAX_SIZE) { //512KB
							log.delete();
						}
					}

					PrintWriter pw = new PrintWriter(startServiceContext.get().openFileOutput("log", Context.MODE_APPEND));
					pw.println(message);
					pw.close();
				}
				catch(Exception e) {
					Log.e(TAG, "Got an error in LogTask", e);
				}
			}
			else {
				Log.e(TAG, "Couldn't get a context in LogTask");
			}
		}
		
		private String createMessage() {
			String[] classNames = ste.getClassName().split("\\.");
			String sourceMethod = (classNames.length > 0 ? classNames[classNames.length - 1] : ste.getClassName()) + "." + 
				ste.getMethodName() + "()";
			
			if(t != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				message += "\n" + sw.toString();
				pw.close();
			}

			try {
				JSONObject json = new JSONObject();
				json.put("LEVEL", printLogLevel(level))
					.put("SOURCE_FILE", ste.getFileName())
					.put("SOURCE_LINE", ste.getLineNumber())
					.put("SOURCE_METHOD", sourceMethod)
					.put("MESSAGE", message)
					.put("TIMESTAMP", new Date().getTime());
				return json.toString();

			}
			catch(Exception e) {
				Log.e(TAG, "Could not build the message in Logger.onHandleIntent()", e);
				return null;
			}
		}
	}
	
	private static void executorLog(String message, Throwable t, int level, boolean logToAndroid) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
		executor.execute(new LogTask(message, t, level, ste, logToAndroid));
	}
	
	public static void setTag(String tag) {
		TAG = tag;
	}
	
	public static void setLogToAndroidDefault(boolean logToAndroidDefault) {
		LOG_TO_ANDROID_DEFAULT = logToAndroidDefault;
	}
	
	public static void d() {
		log("", null, Log.DEBUG, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void d(String message) {
		log(message, null, Log.DEBUG, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void d(String message, Throwable t) {
		log(message, t, Log.DEBUG, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void d(String message, boolean logToAndroid) {
		log(message, null, Log.DEBUG, logToAndroid);
	}
	
	public static void d(String message, Throwable t, boolean logToAndroid) {
		log(message, t, Log.DEBUG, logToAndroid);
	}
	
	public static void e() {
		log("", null, Log.ERROR, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void e(String message) {
		log(message, null, Log.ERROR, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void e(String message, Throwable t) {
		log(message, t, Log.ERROR, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void e(String message, boolean logToAndroid) {
		log(message, null, Log.ERROR, logToAndroid);
	}
	
	public static void e(String message, Throwable t, boolean logToAndroid) {
		log(message, t, Log.ERROR, logToAndroid);
	}
	
	public static void i() {
		log("", null, Log.INFO, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void i(String message) {
		log(message, null, Log.INFO, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void i(String message, Throwable t) {
		log(message, t, Log.INFO, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void i(String message, boolean logToAndroid) {
		log(message, null, Log.INFO, logToAndroid);
	}
	
	public static void i(String message, Throwable t, boolean logToAndroid) {
		log(message, t, Log.INFO, logToAndroid);
	}
	
	public static void v() {
		log("", null, Log.VERBOSE, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void v(String message) {
		log(message, null, Log.VERBOSE, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void v(String message, Throwable t) {
		log(message, t, Log.VERBOSE, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void v(String message, boolean logToAndroid) {
		log(message, null, Log.VERBOSE, logToAndroid);
	}
	
	public static void v(String message, Throwable t, boolean logToAndroid) {
		log(message, t, Log.VERBOSE, logToAndroid);
	}
	
	public static void w() {
		log("", null, Log.WARN, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void w(String message) {
		log(message, null, Log.WARN, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void w(String message, Throwable t) {
		log(message, t, Log.WARN, LOG_TO_ANDROID_DEFAULT);
	}
	
	public static void w(String message, boolean logToAndroid) {
		log(message, null, Log.WARN, logToAndroid);
	}
	
	public static void w(String message, Throwable t, boolean logToAndroid) {
		log(message, t, Log.WARN, logToAndroid);
	}
	
	private static void logToAndroid(String message, Throwable t, int level) {
		switch(level) {
			case Log.ASSERT:
				System.out.println(message);
				break;
			case Log.DEBUG:
				if(t == null) {
					Log.d(TAG, message);
				}
				else {
					Log.d(TAG, message, t);
				}
				break;
			case Log.ERROR:
				if(t == null) {
					Log.e(TAG, message);
				}
				else {
					Log.e(TAG, message, t);
				}
				break;
			case Log.INFO:
				if(t == null) {
					Log.i(TAG, message);
				}
				else {
					Log.i(TAG, message, t);
				}
				break;
			case Log.VERBOSE:
				if(t == null) {
					Log.v(TAG, message);
				}
				else {
					Log.v(TAG, message, t);
				}
				break;
			case Log.WARN:
				if(t == null) {
					Log.w(TAG, message);
				}
				else {
					Log.w(TAG, message, t);
				}
				break;
		}
	}
	
	private static String printLogLevel(int level) {
		switch(level) {
			case Log.ASSERT:
				return "ASSERT";
			case Log.DEBUG:
				return "DEBUG";
			case Log.ERROR:
				return "ERROR";
			case Log.INFO:
				return "INFO";
			case Log.VERBOSE:
				return "VERBOSE";
			case Log.WARN:
				return "WARN";
			default:
				return "OTHER";
		}
	}
}
