package com.cpsolutions.android.net;

import java.net.MalformedURLException;

import android.app.Service;

import com.cpsolutions.net.AbsBackgroundHttpGet;

public abstract class AbsServiceFileDownloaderGet extends AbsBackgroundHttpGet {
	
	private Service service;

	public AbsServiceFileDownloaderGet(String url, Service service) throws MalformedURLException {
		super(url);

		this.service = service;
	}
	
	public final Service getService() {
		return service;
	}

}
