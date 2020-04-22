package com.hy.library_download.db;

import android.os.Parcel;
import android.os.Parcelable;

public class HistoryEntity implements Parcelable {
    private int h_id;
    private String h_name;
    private String h_path;
    private String h_url;

    public HistoryEntity(){}

    public HistoryEntity(String h_name,String h_path,String h_url){
        this.h_name = h_name;
        this.h_path = h_path;
        this.h_url = h_url;
    }

    public int getH_id() {
        return h_id;
    }

    public void setH_id(int h_id) {
        this.h_id = h_id;
    }

    public String getH_name() {
        return h_name;
    }

    public void setH_name(String h_name) {
        this.h_name = h_name;
    }

    public String getH_path() {
        return h_path;
    }

    public void setH_path(String h_path) {
        this.h_path = h_path;
    }

    public String getH_url() {
        return h_url;
    }

    public void setH_url(String h_url) {
        this.h_url = h_url;
    }

    protected HistoryEntity(Parcel in) {
        h_id = in.readInt();
        h_name = in.readString();
        h_path = in.readString();
        h_url = in.readString();
    }

    public static final Creator<HistoryEntity> CREATOR = new Creator<HistoryEntity>() {
        @Override
        public HistoryEntity createFromParcel(Parcel in) {
            return new HistoryEntity(in);
        }

        @Override
        public HistoryEntity[] newArray(int size) {
            return new HistoryEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(h_id);
        parcel.writeString(h_name);
        parcel.writeString(h_path);
        parcel.writeString(h_url);
    }
}
