package com.hy.library_download.view.base;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hy.library_download.DownloadConfig;
import com.hy.library_download.R;
import com.hy.library_download.db.TaskEntity;
import com.hy.library_download.task.TaskManager;
import com.hy.library_download.widget.MagicCircleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class DownloadBaseAdapter<H extends DownloadBaseBean> extends BaseQuickAdapter<H , BaseViewHolder> {

    public DownloadBaseAdapter(int layoutResId, @Nullable List<H> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, H item) {
        myConvert(helper,item);
    }

    protected abstract void myConvert(BaseViewHolder helper, H item);

    protected abstract H getBaseBean(TaskEntity task);

    private int getPos(int taskId){
        if(taskId<0)
            return -1;
        for(int pos=0;pos<getData().size();pos++){
            H bean = getData().get(pos);
            if(taskId == bean.getId())
                return pos;
        }
        return -1;
    }

    public void refreshAdapterData(){
        getData().clear();
        ArrayList<TaskEntity> taskList = TaskManager.getInstance().getAllTaskEntity();
        for(TaskEntity task:taskList)
            addData(getBaseBean(task));
        notifyDataSetChanged();
    }

    public void addDownloadItem(Intent intent) {
        String task_url = intent.getStringExtra("Url");
        if(TextUtils.isEmpty(task_url)){
            Toast.makeText(mContext,"任务已存在",Toast.LENGTH_LONG).show();
        } else {
            TaskEntity task = TaskManager.getInstance().getTaskEntity(task_url);
            addData(getBaseBean(task));
        }
    }


    public void startDownloadItem(Intent intent) {
        int taskId = intent.getIntExtra("Id",-2);
        String task_name = intent.getStringExtra("Name");
        String task_url = intent.getStringExtra("Url");
        if(taskId>=0){
            for(int pos=0;pos<getData().size();pos++){
                DownloadBaseBean bean = getData().get(pos);
                if(!TextUtils.isEmpty(bean.getUrl()) && !TextUtils.isEmpty(task_url) && bean.getUrl().equals(task_url)){
                    bean.setId(taskId);
                    bean.setStatus(DownloadConfig.RUN_FLAG);
                    break;
                }
            }
        } else {
            refreshAdapterData();
        }
    }

    public void stopDownloadItem(Intent intent){
        int taskId = intent.getIntExtra("Id",-2);
        if(taskId>=0){
            int pos = getPos(taskId);
            if (pos>=0 && pos < getData().size()) {
                getData().get(pos).setStatus(DownloadConfig.STOP_FLAG);
                notifyItemChanged(pos);
            }
        }else if(taskId==-1) {
            for(int pos=0;pos<getData().size();pos++)
                getData().get(pos).setStatus(DownloadConfig.STOP_FLAG);
            notifyDataSetChanged();
        }
    }

    public void cancelDownloadItem(Intent intent){
        int taskId = intent.getIntExtra("Id",-2);
        if(taskId>=0){
            int pos = getPos(taskId);
            if (pos>=0 && pos < getData().size())
                remove(pos);
        }else if(taskId==-1) {
            getData().clear();
            notifyDataSetChanged();
        }
    }


    public void completerDownloadItem(Intent intent) {
        int taskId = intent.getIntExtra("Id",-2);
        String name = intent.getStringExtra("Name");
        String path = intent.getStringExtra("Path");
        int pos = getPos(taskId);
        if (pos>=0 && pos < getData().size()){
            remove(pos);
        } else
            Log.i("测试", "完成，未找到该任务：" + pos+"，"+taskId);
    }

    public void errorDownloadItem(Intent intent) {
        int taskId = intent.getIntExtra("Id",-2);
        int pos = getPos(taskId);
        if (pos>=0 && pos < getData().size()){
            getData().get(pos).setStatus(DownloadConfig.ERROR_FLAG);
            notifyItemChanged(pos);
        } else
            Log.i("测试", "错误，未找到该任务：" + pos+"，"+taskId);
    }

    public void progressDownloadItem(Intent intent){
        int taskId = intent.getIntExtra("Id",-2);
        double[][] progress = (double[][]) intent.getSerializableExtra("Progress");
        if(progress !=null){
            int pos = getPos(taskId);
            if (pos>=0 && pos < getData().size()){
                getData().get(pos).setPos(progress);
                notifyItemChanged(pos);
            } else {
                TaskManager.getInstance().pauseItem(taskId);
            }
        }else
            Log.i("测试","QueueEntity是空的");
    }
}
