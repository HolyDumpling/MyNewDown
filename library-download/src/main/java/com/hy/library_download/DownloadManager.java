package com.hy.library_download;

import android.content.Context;
import com.hy.library_download.task.TaskManager;
import com.hy.library_download.view.base.DownloadBaseBean;


public class DownloadManager {
    private DownloadManager() { }
    private static class DownloadManagerHolder { private static final DownloadManager INSTANCE = new DownloadManager();}
    public static  DownloadManager getInstance() { return DownloadManager.DownloadManagerHolder.INSTANCE; }
    //下必须在开启下载任务之前先执行一遍，最好放在application中
    public void init(Context context) throws Exception { TaskManager.getInstance().init(context); }
    //开始全部
    public void startAll(){ TaskManager.getInstance().startAll(); }
    //暂停全部
    public void pauseAll(){ TaskManager.getInstance().pauseAll(); }
    //取消全部
    public void cancelAll(){ TaskManager.getInstance().cancelAll(); }
    //添加一项
    public void addItem(DownloadBaseBean baseBean){ TaskManager.getInstance().addItem(baseBean); }
    //强制开始一项
    public void startItem_Forcibly(int task_id){ TaskManager.getInstance().startItem_Forcibly(task_id); }
    //开始一项（若队列已满则等待）
    public void startItem(int task_id){ TaskManager.getInstance().startItem(task_id); }
    //暂停一项
    public void pauseItem(int task_id){ TaskManager.getInstance().pauseItem(task_id); }
    //取消一项
    public void cancelItem(int task_id){ TaskManager.getInstance().cancelItem(task_id); }
}
