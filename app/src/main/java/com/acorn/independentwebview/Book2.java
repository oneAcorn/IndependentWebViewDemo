package com.acorn.independentwebview;

import android.os.Parcel;
import android.os.Parcelable;

public class Book2 implements Parcelable {
    private String name;

    protected Book2(Parcel in) {
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Book2> CREATOR = new Creator<Book2>() {
        @Override
        public Book2 createFromParcel(Parcel in) {
            return new Book2(in);
        }

        @Override
        public Book2[] newArray(int size) {
            return new Book2[size];
        }
    };
}
