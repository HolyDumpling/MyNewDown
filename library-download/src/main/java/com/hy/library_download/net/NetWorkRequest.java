package com.hy.library_download.net;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class NetWorkRequest {
    private NetWorkRequest() { }
    private static class NetWorkRequestHolder{ private static final NetWorkRequest sInstance = new NetWorkRequest();}
    public static NetWorkRequest getInstance() { return NetWorkRequestHolder.sInstance; }

    public Context mContext;
    private Retrofit mRetrofit;
    private OkHttpClient mOkHttpClient;
    private DownLoadService mDownLoadService;

    /**
     * 初始化Retrofit
     *
     * @param context
     */
    public NetWorkRequest init(Context context, String baseURL) {
        this.mContext = context;
        synchronized (NetWorkRequest.this) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .cache(new Cache(new File(context.getExternalCacheDir(), "http_cache"), 1024 * 1024 * 100))
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)//主机地址
                    .client(mOkHttpClient)
                    .build();
            mDownLoadService = mRetrofit.create(DownLoadService.class);
        }
        return this;
    }

    public DownLoadService getDownLoadService() {
        return mDownLoadService;
    }

}

