package com.miscell.glasswiping.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chenjishi on 14-1-9.
 */
public class Feed implements Parcelable {
    public String title;
    public String url;
    public String imageUrl;

    public Feed() {

    }

    public Feed(Parcel in) {
        title = in.readString();
        url = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(imageUrl);
    }
}
