package com.example.nick.popularmovies;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by NICK on 8/12/2015.
 * A video trailer
 */
public class Trailer implements Parcelable{

    private static final String KEY_ID = "id";
    private static final String KEY_KEY = "key";
    private static final String KEY_NAME = "name";

    public int id;
    public String key;
    public String name;
    public static final Parcelable.Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();

            Trailer trailer = new Trailer();

            trailer.id          = bundle.getInt(KEY_ID, 0);
            trailer.key         = bundle.getString(KEY_KEY, null);
            trailer.name        = bundle.getString(KEY_NAME, null);

            return trailer;
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ID, id);
        bundle.putString(KEY_NAME, name);
        bundle.putString(KEY_KEY, key);
        dest.writeBundle(bundle);
    }
}
