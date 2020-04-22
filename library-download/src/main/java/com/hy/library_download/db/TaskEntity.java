package com.hy.library_download.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.hy.library_download.DownloadConfig;

public class TaskEntity implements Parcelable {
    //队列ID
    public int task_id;
    //队列名称
    public String task_name;
    //队列状态
    public int task_status = DownloadConfig.WAIT_FLAG;
    //下载链接
    public String task_url;
    //断点时间，用于确定上次断点后文件没有更改，可以断点续传
    public String task_lastModify;
    //任务总大小
    public long task_total;
    //下载优先级
    public int task_priority;
    //是否支持断点续传
    public boolean isSupportMulti;

    public TaskEntity(){ }


    public TaskEntity(String task_name,String url) {
        this.task_name = task_name;
        this.task_url = url;
    }

    public void setTaskEntity(TaskEntity taskEntity){
        this.task_id = taskEntity.task_id;
        this.task_name = taskEntity.task_name;
        this.task_status = taskEntity.task_status;
        this.task_url = taskEntity.task_url;
        this.task_priority = taskEntity.task_priority;
        this.task_lastModify = taskEntity.task_lastModify;
        this.task_total = taskEntity.task_total;
    }

    private TaskEntity(Parcel in) {
        task_id = in.readInt();
        task_name = in.readString();
        task_status = in.readInt();
        task_url = in.readString();
        task_priority = in.readInt();
        task_lastModify = in.readString();
        task_total = in.readLong();
    }

    public static final Parcelable.Creator<TaskEntity> CREATOR = new Parcelable.Creator<TaskEntity>() {
        @Override
        public TaskEntity createFromParcel(Parcel in) { return new TaskEntity(in); }
        @Override
        public TaskEntity[] newArray(int size) { return new TaskEntity[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.task_id);
        parcel.writeString(this.task_name);
        parcel.writeInt(this.task_status);
        parcel.writeString(this.task_url);
        parcel.writeInt(this.task_priority);
        parcel.writeString(this.task_lastModify);
        parcel.writeLong(this.task_total);
    }

}
