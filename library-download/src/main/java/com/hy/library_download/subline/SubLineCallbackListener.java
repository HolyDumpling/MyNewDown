package com.hy.library_download.subline;

import com.hy.library_download.db.SubLineEntity;

public interface SubLineCallbackListener {
    void onStart(SubLineEntity subLineEntity);
    void onStop(SubLineEntity subLineEntity);
    void onDownLoading(SubLineEntity subLineEntity);
    void onCompleted(SubLineEntity subLineEntity);
    void onError(SubLineEntity subLineEntity, Throwable throwable);
}
