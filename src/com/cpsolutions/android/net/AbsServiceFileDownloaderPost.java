package com.cpsolutions.android.net;

import java.net.MalformedURLException;

import android.app.Service;

import com.cpsolutions.net.AbsBackgroundHttpPost;

public abstract class AbsServiceFileDownloaderPost extends AbsBackgroundHttpPost {
	
	private Service service;

	public AbsServiceFileDownloaderPost(String url, Service service) throws MalformedURLException {
		super(url);

		this.service = service;
	}
	
	public final Service getService() {
		return service;
	}

}