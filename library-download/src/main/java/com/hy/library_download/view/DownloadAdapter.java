package com.hy.library_download.view;

import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseViewHolder;
import com.hy.library_download.R;
import com.hy.library_download.db.TaskEntity;
import com.hy.library_download.view.base.DownloadBaseAdapter;
import com.hy.library_download.widget.MagicCircleView;

import java.util.List;

public class DownloadAdapter extends DownloadBaseAdapter<DownloadBean> {

    public DownloadAdapter(@Nullable List<DownloadBean> data) {
        super(R.layout.adapter_download,data);
    }

    @Override
    protected void myConvert(BaseViewHolder helper, DownloadBean item) {
        TextView item_tv_title = helper.getView(R.id.item_tv_title);
        item_tv_title.setText(item.getTask_name());
        MagicCircleView mcv_progress = helper.getView(R.id.mcv_progress);
        mcv_progress.setStatus(item.getStatus());
        mcv_progress.setProgressRate(item.getPos());
        helper.addOnClickListener(R.id.content);
        helper.addOnClickListener(R.id.item_tv_cancel);
    }

    @Override
    protected DownloadBean getBaseBean(TaskEntity task) {
        DownloadBean downloadBean = new DownloadBean(task.task_name,task.task_url);
        downloadBean.setId(task.task_id);
        downloadBean.setStatus(task.task_status);
        return downloadBean;
    }
}
