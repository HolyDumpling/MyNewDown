package com.hy.library_download.task;

import com.hy.library_download.db.TaskEntity;

public interface TaskCallbackListener {
    void onStart(int taskId,String taskUrl,String taskName);
    void onCancel(boolean isAll,int taskId);
    void onStop(boolean isAll,int taskId);
    void onDownLoading(int taskId,double[][] progress);
    void onCompleted(int taskId);
    void onError(int taskId, Throwable throwable);
}
