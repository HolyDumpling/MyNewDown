package com.hy.library_download.task;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.hy.library_download.DownloadConfig;
import com.hy.library_download.db.DatabaseHelper;
import com.hy.library_download.db.TaskEntity;
import com.hy.library_download.net.NetWorkRequest;
import com.hy.library_download.util.Utils;
import com.hy.library_download.view.base.DownloadBaseBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskManager {
    private Context mContext;
    //单位时间内下载量（用于判断是否调整下载顺序）
    private static int UNITBYTE_PER_SECOND = DownloadConfig.REGULATORS_SECOND * 400 * 1024;
    private TaskManager() { }
    private static class TaskManagerHolder { private static final TaskManager INSTANCE = new TaskManager();}
    public static  TaskManager getInstance() { return TaskManagerHolder.INSTANCE; }
    public void init(Context context) throws Exception {
        this.mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
        NetWorkRequest.getInstance().init(mContext,DownloadConfig.ROOT_URL);
        DownloadConfig.setExternalCacheDir(mContext);
        if(TextUtils.isEmpty(DownloadConfig.getExternalCacheDir()))
            throw new Exception("获取路径失败");
        File cacheFile = new File(DownloadConfig.getExternalCacheDir());
        if(!cacheFile.exists())
            cacheFile.mkdirs();
        if(!cacheFile.exists())
            throw new Exception("创建缓存目录失败");
    }

    private DatabaseHelper mDatabaseHelper;
    //线程池
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    //下载线程Task
    private Map<Integer, TaskRequest> mTaskRequestMap = new ConcurrentHashMap<>();
    //下载回调
    private TaskCallbackListener taskCallbackListener = new TaskCallbackListener() {
        @Override
        public synchronized void onStart(int taskId,String taskUrl,String taskName) {
            sendBroadcast_Start(taskId,taskUrl,taskName);
        }
        @Override
        public synchronized void onCancel(boolean isAll,int taskId) {
            mDatabaseHelper.deleteSubLine(taskId);
            mDatabaseHelper.deleteTask(taskId);
            mTaskRequestMap.remove(taskId);
            if(!isAll) {
                sendBroadcast_Cancel(taskId);
                startNext();
            }else {
                if( mTaskRequestMap.isEmpty() ){
                    mDatabaseHelper.deleteAllSubLine();
                    mDatabaseHelper.deleteAllTask();
                }
                sendBroadcast_Cancel(-1);
            }
        }

        @Override
        public synchronized void onStop(boolean isAll,int taskId) {
            mDatabaseHelper.updataTaskStatus(taskId,DownloadConfig.STOP_FLAG);
            mTaskRequestMap.remove(taskId);
            if(!isAll) {
                sendBroadcast_Stop(taskId);
                startNext();
            } else {
                if(mTaskRequestMap.isEmpty())
                    mDatabaseHelper.stopAllTask();
                sendBroadcast_Stop(-1);
            }
        }

        @Override
        public synchronized void onDownLoading(int taskId,double[][] progress) { sendBroadcast_ProgressChange(taskId,progress); }

        @Override
        public synchronized void onCompleted(int taskId) {
            TaskEntity task = mDatabaseHelper.queryTask(taskId);
            Utils.moveFile(DownloadConfig.getExternalCacheFilePath(task.task_name),DownloadConfig.getExternalDownloadDir());
            try {
                String md5 = Utils.getFileMD5String(new File(DownloadConfig.getExternalDownloadFilePath(task.task_name)));
                Log.i("测试",task.task_name+"的MD5校验码是："+md5);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("测试","校验MD5出现问题："+e.getMessage());
            }
            mDatabaseHelper.insertHistory(task.task_name,DownloadConfig.getExternalDownloadFilePath(task.task_name),task.task_url);
            mDatabaseHelper.deleteSubLine(taskId);
            mDatabaseHelper.deleteTask(taskId);
            mTaskRequestMap.remove(taskId);
            sendBroadcast_Completed(taskId,task.task_name,DownloadConfig.getExternalDownloadFilePath(task.task_name));
            startNext();
        }

        @Override
        public synchronized void onError(int taskId, Throwable throwable) {
            throwable.printStackTrace();
            Log.i("测试","下载插件出现错误："+throwable.getMessage());
            pauseItem(taskId); sendBroadcast_Error(taskId);
            startNext();
        }
    };

    private synchronized void startNext(){
        ArrayList<TaskEntity> taskList = mDatabaseHelper.queryWaitTask(1);
        if(taskList.size()>0)
            startItem(taskList.get(0));
    }

    //监管者
    private TimerTask regulatorsTask;
    //监管者
    private Timer regulatorsTimer;

    //开始监管
    private void startRegulators() {
        stopRegulators();
        if(mDatabaseHelper.countTask(DownloadConfig.WAIT_FLAG) > 0){
            regulatorsTimer = new Timer();
            regulatorsTask = new TimerTask(){
                @Override
                public void run() {
                    synchronized (getInstance()) {
                        System.out.println("线程名："+Thread.currentThread().getName()+"====="+"执行监督");
                        //若小于最大任务数，取消监管
                        if(mDatabaseHelper.countTask(DownloadConfig.WAIT_FLAG) == 0){
                            stopRegulators();
                            return;
                        }
                        //执行监管
                        if (!mTaskRequestMap.isEmpty()) {
                            Set<Map.Entry<Integer, TaskRequest>> keyEntrySet = mTaskRequestMap.entrySet();
                            for (Map.Entry<Integer, TaskRequest> me : keyEntrySet) {
                                if(me.getValue().task_lastdowned!=-1 && (me.getValue().task_nowdowned - me.getValue().task_lastdowned)<UNITBYTE_PER_SECOND) {
                                    ArrayList<TaskEntity> newTask = mDatabaseHelper.queryWaitTask(1);
                                    if(newTask.size()>0){
                                        mDatabaseHelper.updataTaskPriority(me.getValue().getTaskId());
                                        pauseItem(me.getValue().getTaskId());
                                    }
                                } else {
                                    me.getValue().task_lastdowned = me.getValue().task_nowdowned;
                                }
                            }
                        }
                    }
                }
            };
            regulatorsTimer.schedule(regulatorsTask,0, DownloadConfig.REGULATORS_SECOND * 1000);
        }
    }
    //停止监管
    private void stopRegulators() {
        if(regulatorsTimer != null){
            regulatorsTimer.cancel();
            regulatorsTimer = null;
            regulatorsTask.cancel();
            regulatorsTask = null;
        }
    }

    //开始全部队列
    public synchronized void startAll() {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        int runNum = DownloadConfig.MAXINUM_TASK - mDatabaseHelper.countTask(DownloadConfig.RUN_FLAG);
        mDatabaseHelper.updataAllTaskReplay();
        ArrayList<TaskEntity> taskList = mDatabaseHelper.queryWaitTask(runNum);
        int i=0;
        for(TaskEntity taskEntity : taskList) {
            if(i<runNum){
                TaskRequest taskRequest = new TaskRequest(mDatabaseHelper, taskEntity.task_id,taskCallbackListener);
                mTaskRequestMap.put(taskEntity.task_id,taskRequest);
            }
        }
        if (!mTaskRequestMap.isEmpty()) {
            Set<Map.Entry<Integer, TaskRequest>> keyEntrySet = mTaskRequestMap.entrySet();
            for (Map.Entry<Integer, TaskRequest> me : keyEntrySet) {
                TaskEntity taskEntity = mDatabaseHelper.queryTask(me.getValue().getTaskId());
                taskEntity.task_status = DownloadConfig.RUN_FLAG;
                mDatabaseHelper.updataTaskStatus(taskEntity.task_id,taskEntity.task_status);
                mExecutorService.submit(me.getValue());
            }
        }
        if(taskList.size() > DownloadConfig.MAXINUM_TASK)
            startRegulators();
        sendBroadcast_Start(-1,"","");
    }
    //暂停全部队列
    public synchronized void pauseAll() {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        if (!mTaskRequestMap.isEmpty()) {
            Set<Map.Entry<Integer, TaskRequest>> keyEntrySet = mTaskRequestMap.entrySet();
            for (Map.Entry<Integer, TaskRequest> me : keyEntrySet)
                me.getValue().stop(true);
        } else {
            mDatabaseHelper.stopAllTask();
            sendBroadcast_Stop(-1);
        }
        stopRegulators();
    }
    //取消全部队列
    public synchronized void cancelAll() {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        if (!mTaskRequestMap.isEmpty()) {
            Set<Map.Entry<Integer, TaskRequest>> keyEntrySet = mTaskRequestMap.entrySet();
            for (Map.Entry<Integer, TaskRequest> me : keyEntrySet)
                me.getValue().cancel(true);
        } else {
            mDatabaseHelper.deleteAllSubLine();
            mDatabaseHelper.deleteAllTask();
            sendBroadcast_Cancel(-1);
        }
        stopRegulators();
    }

    //添加单项
    public synchronized void addItem(DownloadBaseBean baseBean) {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        if (mDatabaseHelper.queryTask(baseBean.getUrl())!=null) {
            //任务已经存在
            sendBroadcast_Add("");
            return;
        }
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskEntity(baseBean.getTaskEntity());
        mDatabaseHelper.insertTask(taskEntity);
        taskEntity.setTaskEntity(mDatabaseHelper.queryTask(taskEntity.task_url));
        if(mDatabaseHelper.countTask(DownloadConfig.RUN_FLAG) < DownloadConfig.MAXINUM_TASK) {
            taskEntity.task_status = DownloadConfig.RUN_FLAG;
            mDatabaseHelper.updataTaskStatus(taskEntity.task_id,taskEntity.task_status);
            TaskRequest taskRequest = new TaskRequest(mDatabaseHelper, taskEntity.task_id,taskCallbackListener);
            mTaskRequestMap.put(taskEntity.task_id,taskRequest);
            mExecutorService.submit(taskRequest);
        } else {
            taskEntity.task_status = DownloadConfig.WAIT_FLAG;
            startRegulators();
        }
        sendBroadcast_Add(taskEntity.task_url);
    }

    public synchronized boolean startItem_Forcibly(int task_id){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        TaskEntity taskEntity = mDatabaseHelper.queryTask(task_id);
        if(taskEntity !=null) {
            if (!mTaskRequestMap.containsKey(taskEntity.task_id)) {
                if(mDatabaseHelper.countTask(DownloadConfig.RUN_FLAG) >= DownloadConfig.MAXINUM_TASK){
                    if (!mTaskRequestMap.isEmpty()) {
                        for (Map.Entry<Integer, TaskRequest> me : mTaskRequestMap.entrySet()) {
                            if(me.getValue()!=null){
                                pauseItem(me.getValue().getTaskId());
                                break;
                            }
                        }
                    }
                }
                taskEntity.task_status = DownloadConfig.RUN_FLAG;
                mDatabaseHelper.updataTaskStatus(taskEntity.task_id,taskEntity.task_status,0);
                TaskRequest taskRequest = new TaskRequest(mDatabaseHelper, taskEntity.task_id,taskCallbackListener );
                mTaskRequestMap.put(taskEntity.task_id,taskRequest);
                mExecutorService.submit(taskRequest);
            }else {
                Log.i("测试","已经启动该任务，请勿重复启动："+taskEntity.task_id);
            }
            return true;
        }
        return false;
    }


    public synchronized boolean startItem(TaskEntity taskEntity) {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        if(taskEntity !=null) {
            if(mDatabaseHelper.countTask(DownloadConfig.RUN_FLAG) < DownloadConfig.MAXINUM_TASK) {
                if (!mTaskRequestMap.containsKey(taskEntity.task_id)) {
                    taskEntity.task_status = DownloadConfig.RUN_FLAG;
                    mDatabaseHelper.updataTaskStatus(taskEntity.task_id,taskEntity.task_status);
                    TaskRequest taskRequest = new TaskRequest(mDatabaseHelper, taskEntity.task_id,taskCallbackListener );
                    mTaskRequestMap.put(taskEntity.task_id,taskRequest);
                    mExecutorService.submit(taskRequest);
                }else {
                    Log.i("测试","已经启动该任务，请勿重复启动："+taskEntity.task_id);
                }
            } else {
                Log.i("测试","任务队列已满，等待空闲："+taskEntity.task_id);
                startRegulators();
            }
            return true;
        }
        return false;
    }

    //开始单项
    public synchronized boolean startItem(int task_id) {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        TaskEntity taskEntity = mDatabaseHelper.queryTask(task_id);
        return startItem(taskEntity);
    }

    //暂停单项
    public synchronized void pauseItem(int task_id) {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        TaskEntity taskEntity = mDatabaseHelper.queryTask(task_id);
        if(taskEntity !=null) {
            if (mTaskRequestMap.containsKey(taskEntity.task_id)) {
                TaskRequest taskRequest = mTaskRequestMap.get(taskEntity.task_id);
                if(taskRequest!=null)
                    taskRequest.stop(false);
            }
        }
        if(mDatabaseHelper.countTask(DownloadConfig.RUN_FLAG) <= DownloadConfig.MAXINUM_TASK)
            stopRegulators();
    }
    //取消单项
    public synchronized void cancelItem(int task_id) {
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        TaskEntity taskEntity = mDatabaseHelper.queryTask(task_id);
        if(taskEntity !=null) {
            if (mTaskRequestMap.containsKey(taskEntity.task_id)) {
                TaskRequest taskRequest = mTaskRequestMap.get(taskEntity.task_id);
                if(taskRequest!=null)
                    taskRequest.cancel(false);
            } else {
                mDatabaseHelper.deleteSubLine(task_id);
                mDatabaseHelper.deleteTask(task_id);
                sendBroadcast_Cancel(task_id);
            }
        }
        if(mDatabaseHelper.countTask(DownloadConfig.RUN_FLAG) <= DownloadConfig.MAXINUM_TASK)
            stopRegulators();
    }

    public synchronized TaskEntity getTaskEntity(int task_id){
        return mDatabaseHelper.queryTask(task_id);
    }

    public synchronized TaskEntity getTaskEntity(String task_url){
        return mDatabaseHelper.queryTask(task_url);
    }

    public synchronized ArrayList<TaskEntity> getAllTaskEntity(){
        return mDatabaseHelper.queryAllTask();
    }

    private void sendBroadcast_Add(String taskUrl){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        Log.i("测试","添加————发送广播开始："+taskUrl);
        Intent intent = new Intent();
        intent.setAction(DownloadConfig.ACTION_DOWNLOAD);
        intent.putExtra("Action",DownloadConfig.CALLBACK_ACTION_ADD);
        intent.putExtra("Url",taskUrl);
        mContext.sendBroadcast(intent);
        Log.i("测试","添加————发送广播结束："+taskUrl);
    }

    private void sendBroadcast_Start(int taskId, String taskUrl, String taskName){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        Intent intent = new Intent();
        intent.setAction(DownloadConfig.ACTION_DOWNLOAD);
        intent.putExtra("Action",DownloadConfig.CALLBACK_ACTION_START);
        intent.putExtra("Id",taskId);
        intent.putExtra("Name",taskName);
        intent.putExtra("Url",taskUrl);
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcast_Stop(int taskId){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        Intent intent = new Intent();
        intent.setAction(DownloadConfig.ACTION_DOWNLOAD);
        intent.putExtra("Action",DownloadConfig.CALLBACK_ACTION_STOP);
        intent.putExtra("Id",taskId);
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcast_Cancel(int taskId){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        Intent intent = new Intent();
        intent.setAction(DownloadConfig.ACTION_DOWNLOAD);
        intent.putExtra("Action",DownloadConfig.CALLBACK_ACTION_CANCEL);
        intent.putExtra("Id",taskId);
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcast_ProgressChange(int taskId,double[][] progress){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");

        Intent intent = new Intent();
        intent.setAction(DownloadConfig.ACTION_DOWNLOAD);
        intent.putExtra("Action",DownloadConfig.CALLBACK_ACTION_PROGRESS);
        intent.putExtra("Id",taskId);
        intent.putExtra("Progress",progress);
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcast_Completed(int taskId, String task_name, String filePath){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        Intent intent = new Intent();
        intent.setAction(DownloadConfig.ACTION_DOWNLOAD);
        intent.putExtra("Action",DownloadConfig.CALLBACK_ACTION_COMPLETED);
        intent.putExtra("Id",taskId);
        intent.putExtra("Name",task_name);
        intent.putExtra("Path",filePath);
        mContext.sendBroadcast(intent);
    }

    private void sendBroadcast_Error(int taskId){
        if(mContext==null || mDatabaseHelper==null)
            throw new NullPointerException("DownloadQueueManager not init!");
        Intent intent = new Intent();
        intent.setAction(DownloadConfig.ACTION_DOWNLOAD);
        intent.putExtra("Action",DownloadConfig.CALLBACK_ACTION_ERROR);
        intent.putExtra("Id",taskId);
        mContext.sendBroadcast(intent);
    }

}
