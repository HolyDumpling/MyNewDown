package com.hy.mynewdown;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hy.library_download.DownloadConfig;
import com.hy.library_download.DownloadManager;
import com.hy.library_download.view.DownloadAdapter;
import com.hy.library_download.view.DownloadBean;
import java.util.ArrayList;
import java.util.Random;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BaseQuickAdapter.OnItemChildClickListener {
    ArrayList<DownloadBean> resourceLibrary;

    TextView tv_add ;
    TextView tv_Refresh ;
    TextView tv_all_Start ;
    TextView tv_all_Stop ;
    TextView tv_all_Cancel ;
    RecyclerView rcv_list ;

    DownloadAdapter downloadAdapter;
    Context mContext;

    private final Object key = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initBroadcast();
        try {
            DownloadManager.getInstance().init(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SQLiteStudioService.instance().start(this);
        ResourceLibrary library = new ResourceLibrary();
        resourceLibrary = library.getResourceLibrary();

        tv_Refresh = findViewById(R.id.tv_Refresh);
        tv_add = findViewById(R.id.tv_add);
        tv_all_Start = findViewById(R.id.tv_all_Start);
        tv_all_Stop = findViewById(R.id.tv_all_Stop);
        tv_all_Cancel = findViewById(R.id.tv_all_Cancel);

        tv_Refresh.setOnClickListener(this);
        tv_add.setOnClickListener(this);
        tv_all_Start.setOnClickListener(this);
        tv_all_Stop.setOnClickListener(this);
        tv_all_Cancel.setOnClickListener(this);

        downloadAdapter = new DownloadAdapter(new ArrayList<>());

        rcv_list = findViewById(R.id.rcv_list);
        rcv_list.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        rcv_list.setAdapter(downloadAdapter);
        downloadAdapter.setOnItemChildClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(downloadAdapter!=null)
            downloadAdapter.refreshAdapterData();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(downloadBroadcast);
        DownloadManager.getInstance().pauseAll();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_Refresh:
                downloadAdapter.refreshAdapterData();
                break;
            case R.id.tv_add:
                DownloadBean bean = resourceLibrary.get(new Random().nextInt(resourceLibrary.size()));
                DownloadManager.getInstance().addItem(bean);
                break;
            case R.id.tv_all_Start:
                DownloadManager.getInstance().startAll();
                break;
            case R.id.tv_all_Stop:
                DownloadManager.getInstance().pauseAll();
                break;
            case R.id.tv_all_Cancel:
                DownloadManager.getInstance().cancelAll();
                break;
        }
    }

    private void initBroadcast() {
        IntentFilter download_IntentFilter = new IntentFilter();
        download_IntentFilter.addAction(DownloadConfig.ACTION_DOWNLOAD);
        registerReceiver(downloadBroadcast,download_IntentFilter);
    }


    BroadcastReceiver downloadBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (key){
                String action = intent.getStringExtra("Action");
                if(action==null)
                    return;
                switch (action){
                    case DownloadConfig.CALLBACK_ACTION_ADD:
                        downloadAdapter.addDownloadItem(intent);
                        break;
                    case DownloadConfig.CALLBACK_ACTION_START:
                        downloadAdapter.startDownloadItem(intent);
                        break;
                    case DownloadConfig.CALLBACK_ACTION_STOP:
                        downloadAdapter.stopDownloadItem(intent);
                        break;
                    case DownloadConfig.CALLBACK_ACTION_CANCEL:
                        downloadAdapter.cancelDownloadItem(intent);
                        break;
                    case DownloadConfig.CALLBACK_ACTION_COMPLETED:
                        downloadAdapter.completerDownloadItem(intent);
                        break;
                    case DownloadConfig.CALLBACK_ACTION_ERROR:
                        downloadAdapter.errorDownloadItem(intent);
                        break;
                    case DownloadConfig.CALLBACK_ACTION_PROGRESS:
                        downloadAdapter.progressDownloadItem(intent);
                        break;
                }
            }
        }
    };

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        DownloadBean bean = (DownloadBean) adapter.getData().get(position);
        switch (view.getId()){
            case R.id.content:
                if(bean.getStatus() == DownloadConfig.RUN_FLAG) {
                    Log.i("测试", "点击暂停按钮：");
                    DownloadManager.getInstance().pauseItem(bean.getId());
                } else if(bean.getStatus() == DownloadConfig.COMPLETER_FLAG){
                    Log.i("测试", "点击完成按钮：");
                } else if(bean.getStatus() == DownloadConfig.WAIT_FLAG){
                    Log.i("测试", "点击强制开始按钮：");
                    DownloadManager.getInstance().startItem_Forcibly(bean.getId());
                } else  {
                    Log.i("测试", "点击开始按钮：");
                    DownloadManager.getInstance().startItem(bean.getId());
                }
                break;
            case R.id.item_tv_cancel:
                Log.i("测试", "点击取消按钮：");
                DownloadManager.getInstance().cancelItem(bean.getId());
                break;
        }
    }
}
