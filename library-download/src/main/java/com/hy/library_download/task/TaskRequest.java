package com.hy.library_download.task;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.hy.library_download.DownloadConfig;
import com.hy.library_download.db.DatabaseHelper;
import com.hy.library_download.db.SubLineEntity;
import com.hy.library_download.db.TaskEntity;
import com.hy.library_download.net.NetWorkRequest;
import com.hy.library_download.subline.SubLineCallbackListener;
import com.hy.library_download.subline.SublineRequest;
import com.hy.library_download.util.ErrorException;
import com.hy.library_download.util.StopException;
import com.hy.library_download.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class TaskRequest implements Runnable {
    private boolean stopFlag = false;
    private boolean cancelFlag = false;
    private int task_id;
    private TaskCallbackListener taskCallbackListener;
    private DatabaseHelper mDatabaseHelper;
    //线程池
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    //下载线程Task
    private Map<Integer, SublineRequest> mSublineMap = new ConcurrentHashMap<>();

    double[][] sublineProgress = new double[DownloadConfig.MAXINUM_SUBLINE][2];
    boolean isSupportMulti = false;

    //队列绩效（单位时间工作量）
    public long task_nowdowned;
    public long task_lastdowned;

    public TaskRequest(DatabaseHelper mDatabaseHelper, int task_id, TaskCallbackListener taskCallbackListener) {
        this.mDatabaseHelper = mDatabaseHelper;
        this.task_id = task_id;
        this.taskCallbackListener = taskCallbackListener;
        this.task_nowdowned = 0;
        this.task_lastdowned = -1;
    }

    public int getTaskId() {
        return task_id;
    }

    boolean isAll = false;

    public void stop(boolean isAll) {
        this.task_nowdowned = 0;
        this.task_lastdowned = -1;
        this.isAll = isAll;
        stopFlag = true;
        if(mSublineMap.isEmpty()) {
            if(cancelFlag)
                taskCallbackListener.onCancel(isAll,task_id);
            else
                taskCallbackListener.onStop(isAll,task_id);
        } else
            stopAllSubline();
    }

    public void cancel(boolean isAll) {
        cancelFlag = true;
        stop(isAll);
    }

    @Override
    public void run() {
        try {
            if(stopFlag) throw new StopException("任务停止");
            int result = requestLastModify();
            if(result == 1){ //存在历史记录，直接开始下载
                Log.i("测试","存在历史记录，直接开始下载：");
                startSubline();
            } else if(result == 2){ //新文件，尝试切包
                Log.i("测试","新文件，尝试切包：");
                cutBagForSubline();
                startSubline();
            } else {
                Log.e("测试","请求文件信息异常："+result);
            }
            if(stopFlag) throw new StopException("任务停止");
        } catch (StopException stop) {
            stopAllSubline();
        }
    }

    private void stopAllSubline() {
        if (!mSublineMap.isEmpty()) {
            Set<Map.Entry<Integer, SublineRequest>> keyEntrySet = mSublineMap.entrySet();
            for (Map.Entry<Integer, SublineRequest> me : keyEntrySet)
                me.getValue().stop();
        }
    }

    private int requestLastModify() throws StopException {
        if(stopFlag) throw new StopException("任务停止");
        int result = -1;
        Response response = null;
        Call<ResponseBody> mResponseCall = null;
        TaskEntity taskEntity = mDatabaseHelper.queryTask(task_id);
        if(taskCallbackListener != null)
            taskCallbackListener.onStart(task_id,taskEntity.task_url,taskEntity.task_name);
        int subCount = mDatabaseHelper.countSubLine(taskEntity.task_id);
        if(subCount>0){//存在子线记录
            mResponseCall = NetWorkRequest.getInstance().getDownLoadService().getHttpHeaderWithIfRange(taskEntity.task_url, taskEntity.task_lastModify, "bytes=" + 0 + "-" + 0);
        } else {//没有子线程记录，作为新纪录重新生成下载
            mResponseCall = NetWorkRequest.getInstance().getDownLoadService().getHttpHeader(taskEntity.task_url, "bytes=" + 0 + "-" + 0);
        }
        try {
            response = mResponseCall.execute();
            if (response.isSuccessful()) {
                Log.i("测试","Header："+response.headers().toString());
                String contentRange = response.headers().get("Content-Range");//Content-Range: bytes 0-169704/169705   说明：当前传输的首字节 - 尾字节/文件总字节
                String contentLength = response.headers().get("Content-Length");//请求返回长度，不应该为空
                String lastModified = response.headers().get("Last-Modified");//最近修改记录
                int code = response.code();//返回码
                Log.i("测试",taskEntity.task_name+",Content-Range："+contentRange+",Content-Length："+contentLength+",Last-Modified："+lastModified+",code："+code);
                if(!TextUtils.isEmpty(contentLength)){
                    if(subCount>0 &&( code == 206 || (code == 200 && TextUtils.isEmpty(contentRange) && contentLength.equals(""+taskEntity.task_total)))){
                        result = 1;
                        mDatabaseHelper.updataTaskStatus(taskEntity.task_id,DownloadConfig.RUN_FLAG);
                    } else {
                        result = 2;
                        //删除文件记录，重新开始下载
                        if(!TextUtils.isEmpty(contentRange)){
                            taskEntity.task_total = Long.parseLong(contentRange.split("/")[1]);
                        }else{
                            taskEntity.task_total = Long.parseLong(contentLength);
                        }
                        taskEntity.task_lastModify = lastModified;
                        mDatabaseHelper.deleteSubLine(taskEntity.task_id);
                        mDatabaseHelper.updataTaskInfo(taskEntity.task_id,DownloadConfig.RUN_FLAG,taskEntity.task_lastModify,taskEntity.task_total);
                    }
                } else
                    throw new ErrorException("服务器返回异常");//获取失败
            }else
                throw new ErrorException("连接失败");//获取失败
        } catch (ErrorException | IOException e) {
            taskCallbackListener.onError(task_id,e);
        } finally {
            Log.i("测试","网络连接关闭了");
            if(response!=null && response.body() != null)
                ((ResponseBody) response.body()).close();
        }
        if(stopFlag) throw new StopException("任务停止");
        return result;
    }

    private void cutBagForSubline() throws StopException {
        if(stopFlag) throw new StopException("任务停止");
        try {
            long beginSize = 0;
            //如果文件大小比单包最小尺寸大，代表可分包
            TaskEntity taskEntity = mDatabaseHelper.queryTask(task_id);
            if(taskEntity.task_total > 0){
                String path = DownloadConfig.getExternalCacheFilePath(taskEntity.task_name);
                if(DownloadConfig.cutAble && taskEntity.task_total > DownloadConfig.MULTILINE_BAG_SIZE){
                    long endSize;
                    int singeSize = (int) Math.ceil(((double)taskEntity.task_total)/ DownloadConfig.MAXINUM_SUBLINE);
                    for(int i=1;i<=DownloadConfig.MAXINUM_SUBLINE;i++){
                        Log.i("测试","创建分线第"+i+"");
                        SubLineEntity subLineEntity = new SubLineEntity();
                        subLineEntity.task_id = taskEntity.task_id;
                        subLineEntity.sl_save_path = path;
                        subLineEntity.sl_url = taskEntity.task_url;
                        subLineEntity.sl_status = DownloadConfig.WAIT_FLAG;
                        subLineEntity.sl_downed_lable = 0;
                        if(i == DownloadConfig.MAXINUM_SUBLINE){
                            subLineEntity.sl_start_lable = beginSize;
                            subLineEntity.sl_end_lable = taskEntity.task_total-1;
                            mDatabaseHelper.insertSubline(subLineEntity);
                        } else {
                            endSize = beginSize + singeSize -1;
                            subLineEntity.sl_start_lable = beginSize;
                            subLineEntity.sl_end_lable = endSize;
                            mDatabaseHelper.insertSubline(subLineEntity);
                            beginSize = endSize+1;
                        }
                    }
                } else {
                    Log.i("测试","不可分，创建分线第"+1+"");
                    SubLineEntity subLineEntity = new SubLineEntity();
                    subLineEntity.task_id = taskEntity.task_id;
                    subLineEntity.sl_save_path = path;
                    subLineEntity.sl_url = taskEntity.task_url;
                    subLineEntity.sl_status = DownloadConfig.WAIT_FLAG;
                    subLineEntity.sl_start_lable = beginSize;
                    subLineEntity.sl_end_lable = taskEntity.task_total-1;
                    subLineEntity.sl_downed_lable = 0;
                    mDatabaseHelper.insertSubline(subLineEntity);
                }
            }else {
                throw new ErrorException("文件大小异常："+taskEntity.task_total);
            }
        }catch (ErrorException e){
            taskCallbackListener.onError(task_id,e);
        }
        if(stopFlag) throw new StopException("任务停止");
    }


    private void startSubline() throws StopException {
        if(stopFlag) throw new StopException("任务停止");
        ArrayList<SubLineEntity> subLineList = mDatabaseHelper.querySubline(task_id);
        if(subLineList.size()>1)
            isSupportMulti = true;
        task_nowdowned = 0;
        task_lastdowned = -1;
        int runCount = 0;
        for (int i=0;i<subLineList.size();i++) {
            SubLineEntity subLine = subLineList.get(i);
            sublineProgress[i][0] = subLine.sl_id;
            sublineProgress[i][1] = calculationProgressRatio(subLine);
            if(stopFlag) throw new StopException("任务停止");
            if(subLine.sl_status != DownloadConfig.COMPLETER_FLAG){
                if(runCount<DownloadConfig.MAXINUM_SUBLINE){
                    SublineRequest downLoadTask = new SublineRequest(mDatabaseHelper,subLine,subLineCallbackListener);
                    //尝试下载
                    runCount++;
                    subLine.sl_status = DownloadConfig.RUN_FLAG;
                    mDatabaseHelper.updataSublineStatus(subLine.sl_id,subLine.sl_status);
                    mExecutorService.submit(downLoadTask);
                    mSublineMap.put(subLine.sl_id,downLoadTask);
                } else {
                    Log.wtf("测试","未曾设想的道路"+runCount+"，"+subLine.sl_id);
                    subLine.sl_status = DownloadConfig.WAIT_FLAG;
                    mDatabaseHelper.updataSublineStatus(subLine.sl_id,subLine.sl_status);
                }
            }
        }
        if(stopFlag) throw new StopException("任务停止");
    }

    private double calculationProgressRatio(SubLineEntity subLineEntity){
        long total = subLineEntity.sl_end_lable - subLineEntity.sl_start_lable;
        return (subLineEntity.sl_downed_lable*1.0/total);
    }


    private SubLineCallbackListener subLineCallbackListener = new SubLineCallbackListener() {
        @Override
        public void onStart(SubLineEntity subLineEntity) { }

        @Override
        public void onStop(SubLineEntity subLineEntity) {
            mSublineMap.remove(subLineEntity.sl_id);
            TaskEntity taskEntity = mDatabaseHelper.queryTask(task_id);
            if(taskCallbackListener!=null && mSublineMap.isEmpty()){
                if(cancelFlag){
                    taskCallbackListener.onCancel(isAll,task_id);
                } else {
                    if(taskEntity.task_status!=DownloadConfig.ERROR_FLAG) {
                        taskCallbackListener.onStop(isAll,task_id);
                    } else {
                        //错误导致的停止已经回调，此处无需回调
                    }
                }
            }
        }

        @Override
        public void onDownLoading(SubLineEntity subLineEntity) {
            if(!stopFlag)
                task_nowdowned = task_nowdowned + subLineEntity.sl_downed_lable;
            for(int i=0;i<sublineProgress.length;i++){
                if(sublineProgress[i][0] == subLineEntity.sl_id)
                    sublineProgress[i][1] = calculationProgressRatio(subLineEntity);
            }
            if(taskCallbackListener!=null){
                if(isSupportMulti){
                    taskCallbackListener.onDownLoading(task_id,sublineProgress);
                } else
                    taskCallbackListener.onDownLoading(task_id,new double[][]{{subLineEntity.sl_id,calculationProgressRatio(subLineEntity)}});
            }
        }

        @Override
        public void onCompleted(SubLineEntity subLineEntity) {
            mSublineMap.remove(subLineEntity.sl_id);
            if(taskCallbackListener!=null && mSublineMap.isEmpty()) {
                taskCallbackListener.onCompleted(task_id);
            }
        }

        @Override
        public void onError(SubLineEntity subLineEntity, Throwable throwable) {
            mDatabaseHelper.updataTaskStatus(task_id,DownloadConfig.ERROR_FLAG);
            mSublineMap.remove(subLineEntity.sl_id);
            stopFlag = true;
            stopAllSubline();
            taskCallbackListener.onError(task_id,throwable);
        }
    };



}
