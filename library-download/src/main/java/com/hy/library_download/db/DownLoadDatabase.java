package com.hy.library_download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.util.Log;

public class DownLoadDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DownloadCacheLog.db";
    //数据库升级，放弃老数据。
    public static final int DB_VERSION = 1;

    public DownLoadDatabase(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.i("测试","创建表");
        db.execSQL(DatabaseConfig.TaskTable.CREATE_TABLE);
        db.execSQL(DatabaseConfig.SublineTable.CREATE_TABLE);
        db.execSQL(DatabaseConfig.HistoryTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("测试","删除表");
        db.execSQL(DatabaseConfig.TaskTable.DROP_SQL);
        db.execSQL(DatabaseConfig.SublineTable.DROP_SQL);
        db.execSQL(DatabaseConfig.HistoryTable.DROP_SQL);
        onCreate(db);
    }

}