package com.hy.library_download;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DownloadConfig {
    //这是1MB的文件大小,作为计量单位常量，不要更改
    private static final int MB_SIZE = 1024 * 1024;

    //同时下载任务数
    public static int MAXINUM_TASK = 4;
    //单个任务子线程数
    public static int MAXINUM_SUBLINE = 3;
    //监管间隔时间，秒
    public static int REGULATORS_SECOND = 60;
    //是否允许开启多线程下载
    public static boolean cutAble = true;
    //缓存目录：SDCard/Android/data/包名/cache/DownloadSwep
    private static final String CACHE_DIR_NAME = "DownloadSwep"+ File.separator;
    //文件目录：SDCard/Download/
    private static final String DOWNLOAD_DIR_NAME = "MyDownload"+ File.separator;
    //回调尺度（一个任务触发多少次回调，对分线回调次数=CALLBACK_SCALE/sublineCount）
    public static final int CALLBACK_SCALE = 100;
    //最小回调阈值（触发回调的最低阈值，比如文件一百K，回调一百次，每次1K，显然过小了）
    public static final int CALLBACK_MIN_THRESHOLD = (int)(MB_SIZE * 0.1);
    //最大回调阈值（触发回调的最低阈值，比如文件一百G，回调一百次，每次1G，显然过大了）
    public static final int CALLBACK_MAX_THRESHOLD = MB_SIZE * 10;
    //多线程分包大小，当大于此数值时，允许多线程分包下载
    public static long MULTILINE_BAG_SIZE = 3 * MB_SIZE;
    //服务器根节点
    public static String ROOT_URL = "http://127.0.0.1";
    //下载的广播名称
    public static final String ACTION_DOWNLOAD = "com.hy.library_download.action.ACTION_DOWNLOAD";



    public static final String CALLBACK_ACTION_ADD = "ADD";
    public static final String CALLBACK_ACTION_START = "START";
    public static final String CALLBACK_ACTION_CANCEL = "CANCEL";
    public static final String CALLBACK_ACTION_STOP = "STOP";
    public static final String CALLBACK_ACTION_PROGRESS = "PROGRESS";
    public static final String CALLBACK_ACTION_COMPLETED = "COMPLETED";
    public static final String CALLBACK_ACTION_ERROR = "ERROR";
    //Task状态
    public static final int WAIT_FLAG = 0;
    public static final int STOP_FLAG = 1;
    public static final int RUN_FLAG = 2;
    public static final int ERROR_FLAG = 3;
    public static final int COMPLETER_FLAG = 4;

    private static  String CACHE_PATH = "";

    public static void setExternalCacheDir(Context context){ CACHE_PATH = context.getExternalCacheDir() + File.separator + CACHE_DIR_NAME; }

    private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DOWNLOAD_DIR_NAME;

    public static String getExternalCacheDir(){ return CACHE_PATH; }
    public static String getExternalDownloadDir(){ return DOWNLOAD_PATH; }

    public static String getExternalCacheFilePath(String fileName){ return DownloadConfig.getExternalCacheDir() + fileName; }

    public static File getExternalCacheFile(String fileName){ return new File(DownloadConfig.getExternalCacheDir() + fileName); }

    public static String getExternalDownloadFilePath(String fileName){ return DownloadConfig.getExternalDownloadDir() + fileName; }

    public static File getExternalDownloadFile(String fileName){ return new File(DownloadConfig.getExternalDownloadDir() + fileName); }


}
