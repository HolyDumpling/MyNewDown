package com.hy.library_download.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.hy.library_download.DownloadConfig;

public class SubLineEntity implements Parcelable {
    //子线ID
    public int sl_id;
    //任务ID
    public int task_id;
    //子线状态
    public int sl_status = DownloadConfig.WAIT_FLAG;
    //保存地址
    public String sl_save_path;
    //下载链接
    public String sl_url;
    //结束位置
    public long sl_end_lable;
    //开始位置
    public long sl_start_lable;
    //已下载位置
    public long sl_downed_lable;


    public SubLineEntity(){ }

    protected SubLineEntity(Parcel in) {
        this.sl_id = in.readInt();
        this.task_id = in.readInt();
        this.sl_status = in.readInt();
        this.sl_url = in.readString();
        this.sl_start_lable = in.readLong();
        this.sl_end_lable = in.readLong();
        this.sl_downed_lable = in.readLong();
        this.sl_save_path = in.readString();
    }

    public static final Parcelable.Creator<SubLineEntity> CREATOR = new Parcelable.Creator<SubLineEntity>() {
        @Override
        public SubLineEntity createFromParcel(Parcel in) {
            return new SubLineEntity(in);
        }

        @Override
        public SubLineEntity[] newArray(int size) {
            return new SubLineEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.sl_id);
        parcel.writeInt(this.task_id);
        parcel.writeInt(this.sl_status);
        parcel.writeString(this.sl_url);
        parcel.writeLong(this.sl_start_lable);
        parcel.writeLong(this.sl_end_lable);
        parcel.writeLong(this.sl_downed_lable);
        parcel.writeString(this.sl_save_path);
    }
}
