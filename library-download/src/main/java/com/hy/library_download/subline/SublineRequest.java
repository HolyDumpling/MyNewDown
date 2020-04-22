package com.hy.library_download.subline;

import android.util.Log;

import com.hy.library_download.DownloadConfig;
import com.hy.library_download.db.DatabaseHelper;
import com.hy.library_download.db.SubLineEntity;
import com.hy.library_download.net.NetWorkRequest;
import com.hy.library_download.util.ErrorException;
import com.hy.library_download.util.StopException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class SublineRequest implements Runnable{
    private boolean stopFlag = false;
    private DatabaseHelper mDatabaseHelper;
    private SubLineCallbackListener subLineCallbackListener;
    private SubLineEntity mSubLineEntity;
    private long mNeedDownSize;
    private long mFileSizeDownloaded;
    private long CALL_BACK_LENGTH;
    private int sublineCount = 0;

    public SublineRequest(DatabaseHelper mDatabaseHelper, SubLineEntity subLineEntity, SubLineCallbackListener subLineCallbackListener){
        this.mDatabaseHelper = mDatabaseHelper;
        this.mSubLineEntity = subLineEntity;
        this.subLineCallbackListener = subLineCallbackListener;
        this.mNeedDownSize = mSubLineEntity.sl_end_lable - (mSubLineEntity.sl_start_lable + mSubLineEntity.sl_downed_lable);
        this.sublineCount = mDatabaseHelper.countSubLine(mSubLineEntity.task_id);
        this.CALL_BACK_LENGTH = (int)((mSubLineEntity.sl_end_lable - mSubLineEntity.sl_start_lable)*sublineCount*1.0/DownloadConfig.CALLBACK_SCALE);
        if(DownloadConfig.CALLBACK_MIN_THRESHOLD>0 && this.CALL_BACK_LENGTH<DownloadConfig.CALLBACK_MIN_THRESHOLD)
            this.CALL_BACK_LENGTH = DownloadConfig.CALLBACK_MIN_THRESHOLD;
        if(DownloadConfig.CALLBACK_MAX_THRESHOLD>0 && this.CALL_BACK_LENGTH>DownloadConfig.CALLBACK_MAX_THRESHOLD)
            this.CALL_BACK_LENGTH = DownloadConfig.CALLBACK_MAX_THRESHOLD;
        Log.i("测试","创建子线：："+subLineEntity.sl_id);
    }

    public SubLineEntity getSubLineEntity(){
        return mSubLineEntity;
    }

    public void stop() { stopFlag = true;}

    @Override
    public void run() {
        try {
            if(stopFlag) throw new StopException("子线停止");
            Call<ResponseBody> mResponseCall;
            if (mSubLineEntity.sl_downed_lable != 0)
                mResponseCall = NetWorkRequest.getInstance().getDownLoadService().downloadFile(mSubLineEntity.sl_url, "bytes=" + (mSubLineEntity.sl_downed_lable + mSubLineEntity.sl_start_lable) + "-" + mSubLineEntity.sl_end_lable);
            else
                mResponseCall = NetWorkRequest.getInstance().getDownLoadService().downloadFile(mSubLineEntity.sl_url, "bytes=" + mSubLineEntity.sl_start_lable + "-" + mSubLineEntity.sl_end_lable);
            ResponseBody result = null;
            try {
                Response response = mResponseCall.execute();
                result = (ResponseBody) response.body();
                if (response.isSuccessful()) {
                    if (writeToFile(result, mSubLineEntity.sl_start_lable, mSubLineEntity.sl_downed_lable)) {
                        mDatabaseHelper.updataSublineStatus(mSubLineEntity.sl_id,DownloadConfig.COMPLETER_FLAG);
                        subLineCallbackListener.onCompleted(mSubLineEntity);
                    }
                } else {
                    subLineCallbackListener.onError(mSubLineEntity,new ErrorException(response.message()));
                }
            } catch (IOException e) {
                subLineCallbackListener.onError(mSubLineEntity,new ErrorException(e.getMessage()));
            } finally {
                if (result != null)
                    result.close();
            }

            if(stopFlag) throw new StopException("子线停止");
        } catch (StopException e){
            mDatabaseHelper.updataSublineStatus(mSubLineEntity.sl_id,DownloadConfig.WAIT_FLAG);
            subLineCallbackListener.onStop(mSubLineEntity);
        }
        Log.i("测试","Subline线程已结束："+mSubLineEntity.sl_id);
    }


    private boolean writeToFile(ResponseBody body, long startSet, long mDownedSet) throws StopException {
        RandomAccessFile oSavedFile = null;
        FileChannel channelOut = null;
        InputStream inputStream = null;
        try {
            File futureStudioIconFile = new File(mSubLineEntity.sl_save_path);
            Log.i("测试","文件目录："+futureStudioIconFile.getAbsolutePath());
            if (!futureStudioIconFile.exists())
                futureStudioIconFile.createNewFile();
            oSavedFile = new RandomAccessFile(futureStudioIconFile, "rw");
            channelOut = oSavedFile.getChannel();
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startSet + mDownedSet, body.contentLength());
            byte[] fileReader = new byte[1024 * 8];
            inputStream = body.byteStream();
            while (mFileSizeDownloaded < mNeedDownSize) {
                if(stopFlag) {
                    mDatabaseHelper.updataSublineStatus(mSubLineEntity.sl_id,mSubLineEntity.sl_status);
                    channelOut.close();
                    oSavedFile.close();
                    inputStream.close();
                    throw new StopException("子线停止");
                }
                int read = inputStream.read(fileReader);
                if (read == -1)
                    break;
                mappedBuffer.put(fileReader, 0, read);
                mFileSizeDownloaded += read;
                if (mFileSizeDownloaded >= CALL_BACK_LENGTH) {
                    mSubLineEntity.sl_downed_lable += mFileSizeDownloaded;
                    mDatabaseHelper.updataSublineDownedLable(mSubLineEntity.sl_id,mSubLineEntity.sl_downed_lable);
                    subLineCallbackListener.onDownLoading(mSubLineEntity);
                    mNeedDownSize -= mFileSizeDownloaded;
                    mFileSizeDownloaded = 0;
                } else if (mNeedDownSize < CALL_BACK_LENGTH && mFileSizeDownloaded - 1 == mNeedDownSize) {
                    mSubLineEntity.sl_downed_lable += mFileSizeDownloaded;
                    mDatabaseHelper.updataSublineDownedLable(mSubLineEntity.sl_id,mSubLineEntity.sl_downed_lable);
                    subLineCallbackListener.onDownLoading(mSubLineEntity);
                    break;
                }
            }
            return true;
        } catch (IOException e) {
            subLineCallbackListener.onError(mSubLineEntity,e);
            return false;
        } finally {
            Log.i("测试","安全关闭文件流：");
            try {
                if (channelOut != null)
                    channelOut.close();
                if (oSavedFile != null)
                    oSavedFile.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                subLineCallbackListener.onError(mSubLineEntity,e);
            }
        }
    }




}
