package com.cpsolutions.android.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableNameValuePair extends NameValuePair implements Parcelable {
	//////////////////////////////
	// Parcelable apis
	//////////////////////////////
	public static final Parcelable.Creator<ParcelableNameValuePair> CREATOR = new Parcelable.Creator<ParcelableNameValuePair>() {
		public ParcelableNameValuePair createFromParcel(Parcel p) {
			return new ParcelableNameValuePair(p);
		}

		public ParcelableNameValuePair[] newArray(int size) {
			return new ParcelableNameValuePair[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel p, int flags) {
		p.writeString(getName());
		p.writeString(getValue());
	}
	//////////////////////////////
	// end Parcelable apis
	//////////////////////////////
	
	public ParcelableNameValuePair(Parcel p) {
        super(p.readString(), p.readString());
    }

}
