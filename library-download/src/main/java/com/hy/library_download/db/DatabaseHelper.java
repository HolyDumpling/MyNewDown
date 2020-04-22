package com.hy.library_download.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hy.library_download.DownloadConfig;
import com.hy.library_download.util.Utils;

import java.util.ArrayList;

public class DatabaseHelper {

    private final DownLoadDatabase mdb_DownLoad;

    public DatabaseHelper(Context context){ mdb_DownLoad = new DownLoadDatabase(context); }

    public synchronized void close(){ mdb_DownLoad.close();}

    public synchronized ArrayList<TaskEntity> queryAllTask(){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        ArrayList<TaskEntity> taskEntiyList = new ArrayList<>();
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                        DatabaseConfig.TaskTable.Columns.TASK_ID,
                        DatabaseConfig.TaskTable.Columns.TASK_STATUS,
                        DatabaseConfig.TaskTable.Columns.TASK_NAME,
                        DatabaseConfig.TaskTable.Columns.TASK_URL,
                        DatabaseConfig.TaskTable.Columns.TASK_LASTMODIFY,
                        DatabaseConfig.TaskTable.Columns.TASK_TOTAL,
                        DatabaseConfig.TaskTable.Columns.TASK_PRIORITY
                },null,null,null,null,DatabaseConfig.TaskTable.Columns.TASK_PRIORITY+" ASC");
        while (cursor.moveToNext()) {
            TaskEntity entity = new TaskEntity();
            entity.task_id = cursor.getInt(0);
            entity.task_status = cursor.getInt(1);
            entity.task_name = cursor.getString(2);
            entity.task_url = cursor.getString(3);
            entity.task_lastModify = cursor.getString(4);
            entity.task_total = cursor.getLong(5);
            entity.task_priority = cursor.getInt(6);
            taskEntiyList.add(entity);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return taskEntiyList;
    }

    public synchronized void updataAllTaskReplay(){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.TaskTable.Columns.TASK_STATUS, DownloadConfig.WAIT_FLAG);
        sqlite.update(DatabaseConfig.TaskTable.TABLE_NAME,  values,
                DatabaseConfig.TaskTable.Columns.TASK_STATUS+"!=?" , new String[]{String.valueOf(DownloadConfig.COMPLETER_FLAG)});
        sqlite.close();
    }

    public synchronized ArrayList<TaskEntity> queryWaitTask(int howMuch){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        ArrayList<TaskEntity> taskEntiyList = new ArrayList<>();
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                DatabaseConfig.TaskTable.Columns.TASK_ID,
                DatabaseConfig.TaskTable.Columns.TASK_STATUS,
                DatabaseConfig.TaskTable.Columns.TASK_NAME,
                DatabaseConfig.TaskTable.Columns.TASK_URL,
                DatabaseConfig.TaskTable.Columns.TASK_LASTMODIFY,
                DatabaseConfig.TaskTable.Columns.TASK_TOTAL,
                DatabaseConfig.TaskTable.Columns.TASK_PRIORITY}
                ,DatabaseConfig.TaskTable.Columns.TASK_STATUS+"=?", new String[]{String.valueOf(DownloadConfig.WAIT_FLAG)},null,null
                , DatabaseConfig.TaskTable.Columns.TASK_PRIORITY+" ASC");
        int i=0;
        while (cursor.moveToNext()) {
            TaskEntity entity = new TaskEntity();
            entity.task_id = cursor.getInt(0);
            entity.task_status = cursor.getInt(1);
            entity.task_name = cursor.getString(2);
            entity.task_url = cursor.getString(3);
            entity.task_lastModify = cursor.getString(4);
            entity.task_total = cursor.getLong(5);
            entity.task_priority = cursor.getInt(6);
            taskEntiyList.add(entity);
            i++;
            if(i==howMuch)
                break;
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return taskEntiyList;
    }

    public synchronized int countTask(int task_status){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{"count(*)"},
                DatabaseConfig.TaskTable.Columns.TASK_STATUS+"=?",new String[]{String.valueOf(task_status)},null,null,null);
        int i=0;
        if (cursor.moveToNext()) {
            i = cursor.getInt(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return i;
    }

    public synchronized TaskEntity queryTask(int task_id){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        TaskEntity entity = null;
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                DatabaseConfig.TaskTable.Columns.TASK_ID,
                DatabaseConfig.TaskTable.Columns.TASK_STATUS,
                DatabaseConfig.TaskTable.Columns.TASK_NAME,
                DatabaseConfig.TaskTable.Columns.TASK_URL,
                DatabaseConfig.TaskTable.Columns.TASK_LASTMODIFY,
                DatabaseConfig.TaskTable.Columns.TASK_TOTAL,
                DatabaseConfig.TaskTable.Columns.TASK_PRIORITY
        },DatabaseConfig.TaskTable.Columns.TASK_ID+"=?",new String[]{String.valueOf(task_id)},null,null,null);
        if (cursor.moveToNext()) {
            entity = new TaskEntity();
            entity.task_id = cursor.getInt(0);
            entity.task_status = cursor.getInt(1);
            entity.task_name = cursor.getString(2);
            entity.task_url = cursor.getString(3);
            entity.task_lastModify = cursor.getString(4);
            entity.task_total = cursor.getLong(5);
            entity.task_priority = cursor.getInt(6);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return entity;
    }

    public synchronized TaskEntity queryTask(String task_url){
        Log.i("测试","添加————查找任务开始");
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        TaskEntity entity = null;
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                DatabaseConfig.TaskTable.Columns.TASK_ID,
                DatabaseConfig.TaskTable.Columns.TASK_STATUS,
                DatabaseConfig.TaskTable.Columns.TASK_NAME,
                DatabaseConfig.TaskTable.Columns.TASK_URL,
                DatabaseConfig.TaskTable.Columns.TASK_LASTMODIFY,
                DatabaseConfig.TaskTable.Columns.TASK_TOTAL,
                DatabaseConfig.TaskTable.Columns.TASK_PRIORITY
        },DatabaseConfig.TaskTable.Columns.TASK_URL+"=?",new String[]{task_url},null,null,null);
        if (cursor.moveToNext()) {
            entity = new TaskEntity();
            entity.task_id = cursor.getInt(0);
            entity.task_status = cursor.getInt(1);
            entity.task_name = cursor.getString(2);
            entity.task_url = cursor.getString(3);
            entity.task_lastModify = cursor.getString(4);
            entity.task_total = cursor.getLong(5);
            entity.task_priority = cursor.getInt(6);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        Log.i("测试","添加————查找任务结束");
        return entity;
    }

    public synchronized void insertTask(TaskEntity task){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.TaskTable.Columns.TASK_STATUS, DownloadConfig.WAIT_FLAG);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_NAME, task.task_name);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_URL, task.task_url);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_LASTMODIFY, task.task_lastModify);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_TOTAL, task.task_total);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_PRIORITY, 0);
        sqlite.insert(DatabaseConfig.TaskTable.TABLE_NAME, null, values);
        sqlite.close();
    }

    public synchronized void updataTaskInfo(int task_id,int task_status,String task_lastModify,long task_total){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.TaskTable.Columns.TASK_STATUS, task_status);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_LASTMODIFY, task_lastModify);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_TOTAL, task_total);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_PRIORITY, 0);
        sqlite.update(DatabaseConfig.TaskTable.TABLE_NAME,  values,
                DatabaseConfig.TaskTable.Columns.TASK_ID+"=?" , new String[]{String.valueOf(task_id)});
        sqlite.close();
    }

    public synchronized void updataTaskStatus(int task_id,int task_status){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.TaskTable.Columns.TASK_STATUS, task_status);
        sqlite.update(DatabaseConfig.TaskTable.TABLE_NAME,  values,
                DatabaseConfig.TaskTable.Columns.TASK_ID+"=?" , new String[]{String.valueOf(task_id)});
        sqlite.close();
    }

    public synchronized void updataTaskStatus(int task_id,int task_status,int task_priority){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.TaskTable.Columns.TASK_STATUS, task_status);
        values.put(DatabaseConfig.TaskTable.Columns.TASK_PRIORITY, task_priority);
        sqlite.update(DatabaseConfig.TaskTable.TABLE_NAME,  values,
                DatabaseConfig.TaskTable.Columns.TASK_ID+"=?" , new String[]{String.valueOf(task_id)});
        sqlite.close();
    }

    public synchronized void updataTaskPriority(int task_id){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        sqlite.execSQL(" update "+DatabaseConfig.TaskTable.TABLE_NAME+
                " set "+DatabaseConfig.TaskTable.Columns.TASK_PRIORITY+"="+DatabaseConfig.TaskTable.Columns.TASK_PRIORITY+"+1"+
                " where "+DatabaseConfig.TaskTable.Columns.TASK_ID+"="+task_id+";");
        sqlite.close();
    }

    public synchronized void stopAllTask(){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.TaskTable.Columns.TASK_STATUS, DownloadConfig.STOP_FLAG);
        sqlite.update(DatabaseConfig.TaskTable.TABLE_NAME,  values,null, null);
        sqlite.close();
    }

    public synchronized void deleteTask(int task_id){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        sqlite.delete(DatabaseConfig.TaskTable.TABLE_NAME, DatabaseConfig.TaskTable.Columns.TASK_ID+"=?" , new String[]{String.valueOf(task_id)});
        sqlite.close();
    }

    public synchronized void deleteAllTask(){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        sqlite.delete(DatabaseConfig.TaskTable.TABLE_NAME, null , null);
        sqlite.close();
    }

    public synchronized void insertSubline(SubLineEntity subLine){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.SublineTable.Columns.TASK_ID, subLine.task_id);
        values.put(DatabaseConfig.SublineTable.Columns.SL_SAVE_PATH, subLine.sl_save_path);
        values.put(DatabaseConfig.SublineTable.Columns.SL_URL, subLine.sl_url);
        values.put(DatabaseConfig.SublineTable.Columns.SL_STATUS, DownloadConfig.WAIT_FLAG);
        values.put(DatabaseConfig.SublineTable.Columns.SL_START_LABLE, subLine.sl_start_lable);
        values.put(DatabaseConfig.SublineTable.Columns.SL_END_LABLE, subLine.sl_end_lable);
        values.put(DatabaseConfig.SublineTable.Columns.SL_DOWNED_LABLE, 0);
        sqlite.insert(DatabaseConfig.SublineTable.TABLE_NAME, null, values);
        sqlite.close();
    }

    public synchronized ArrayList<SubLineEntity> querySubline(int task_id){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        ArrayList<SubLineEntity> subLineList = new ArrayList<>();
        Cursor cursor = sqlite.query(DatabaseConfig.SublineTable.TABLE_NAME, new String[]{
                DatabaseConfig.SublineTable.Columns.SL_ID,
                DatabaseConfig.SublineTable.Columns.TASK_ID,
                DatabaseConfig.SublineTable.Columns.SL_SAVE_PATH,
                DatabaseConfig.SublineTable.Columns.SL_URL,
                DatabaseConfig.SublineTable.Columns.SL_STATUS,
                DatabaseConfig.SublineTable.Columns.SL_START_LABLE,
                DatabaseConfig.SublineTable.Columns.SL_END_LABLE,
                DatabaseConfig.SublineTable.Columns.SL_DOWNED_LABLE
        },DatabaseConfig.SublineTable.Columns.TASK_ID+"=?",new String[]{String.valueOf(task_id)},null,null,null);
        while (cursor.moveToNext()) {
            SubLineEntity entity = new SubLineEntity();
            entity.sl_id = cursor.getInt(0);
            entity.task_id = cursor.getInt(1);
            entity.sl_save_path = cursor.getString(2);
            entity.sl_url = cursor.getString(3);
            entity.sl_status = cursor.getInt(4);
            entity.sl_start_lable = cursor.getLong(5);
            entity.sl_end_lable = cursor.getLong(6);
            entity.sl_downed_lable = cursor.getLong(7);
            subLineList.add(entity);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return subLineList;
    }

    public synchronized ArrayList<SubLineEntity> queryWaitSubline(int howMuch){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        ArrayList<SubLineEntity> subLineList = new ArrayList<>();
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                DatabaseConfig.SublineTable.Columns.SL_ID,
                DatabaseConfig.SublineTable.Columns.TASK_ID,
                DatabaseConfig.SublineTable.Columns.SL_SAVE_PATH,
                DatabaseConfig.SublineTable.Columns.SL_URL,
                DatabaseConfig.SublineTable.Columns.SL_STATUS,
                DatabaseConfig.SublineTable.Columns.SL_START_LABLE,
                DatabaseConfig.SublineTable.Columns.SL_END_LABLE,
                DatabaseConfig.SublineTable.Columns.SL_DOWNED_LABLE
        },DatabaseConfig.SublineTable.Columns.SL_STATUS+"=?",new String[]{String.valueOf(DownloadConfig.WAIT_FLAG)},null,null,null);
        int i=0;
        while (cursor.moveToNext()) {
            SubLineEntity entity = new SubLineEntity();
            entity.sl_id = cursor.getInt(0);
            entity.task_id = cursor.getInt(1);
            entity.sl_save_path = cursor.getString(2);
            entity.sl_url = cursor.getString(3);
            entity.sl_status = cursor.getInt(4);
            entity.sl_start_lable = cursor.getLong(5);
            entity.sl_end_lable = cursor.getLong(6);
            entity.sl_downed_lable = cursor.getLong(7);
            subLineList.add(entity);
            i++;
            if(i==howMuch)
                break;
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return subLineList;
    }

    public synchronized int countSubLine(int task_id){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        Cursor cursor = sqlite.query(DatabaseConfig.SublineTable.TABLE_NAME, new String[]{"count(*)"},
                DatabaseConfig.SublineTable.Columns.TASK_ID+"=?",new String[]{String.valueOf(task_id)},null,null,null);
        int i=0;
        if (cursor.moveToNext()) {
            i = cursor.getInt(0);
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return i;
    }

    public synchronized void updataSublineStatus(int sl_id,int sl_status){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.SublineTable.Columns.SL_STATUS, sl_status);
        sqlite.update(DatabaseConfig.SublineTable.TABLE_NAME,  values,
                DatabaseConfig.SublineTable.Columns.SL_ID+"=? AND "+ DatabaseConfig.SublineTable.Columns.SL_STATUS+"!=?" , new String[]{String.valueOf(sl_id),String.valueOf(DownloadConfig.COMPLETER_FLAG)});
        sqlite.close();
    }

    public synchronized void updataSublineDownedLable(int sl_id,long sl_downed_lable){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.SublineTable.Columns.SL_DOWNED_LABLE, sl_downed_lable);
        sqlite.update(DatabaseConfig.SublineTable.TABLE_NAME,  values,
                DatabaseConfig.SublineTable.Columns.SL_ID+"=?" , new String[]{String.valueOf(sl_id)});
        sqlite.close();
    }


    public synchronized void deleteSubLine(int task_id){
        TaskEntity task = queryTask(task_id);
        if(task!=null){
            Utils.removeFile(DownloadConfig.getExternalCacheFilePath(task.task_name));
            SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
            sqlite.delete(DatabaseConfig.SublineTable.TABLE_NAME, DatabaseConfig.SublineTable.Columns.TASK_ID+"=?" , new String[]{String.valueOf(task_id)});
            sqlite.close();
        }
    }

    public synchronized void deleteAllSubLine(){
        ArrayList<TaskEntity> taskList = queryAllTask();
        for(TaskEntity task:taskList)
            Utils.removeFile(DownloadConfig.getExternalCacheFilePath(task.task_name));

        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        sqlite.delete(DatabaseConfig.SublineTable.TABLE_NAME, null , null);
        sqlite.close();
    }


    public synchronized void insertHistory(String fileName,String filePath,String url){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.HistoryTable.Columns.H_NAME, fileName);
        values.put(DatabaseConfig.HistoryTable.Columns.H_PATH, filePath);
        values.put(DatabaseConfig.HistoryTable.Columns.H_URL, url);
        sqlite.insert(DatabaseConfig.HistoryTable.TABLE_NAME, null, values);
        sqlite.close();
    }

    public synchronized HistoryEntity queryHistoryForName(String fileName){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        HistoryEntity entity = null;
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                DatabaseConfig.HistoryTable.Columns.H_ID,
                DatabaseConfig.HistoryTable.Columns.H_NAME,
                DatabaseConfig.HistoryTable.Columns.H_PATH,
                DatabaseConfig.HistoryTable.Columns.H_URL,
        },DatabaseConfig.HistoryTable.Columns.H_NAME+"=?",new String[]{fileName},null,null,null);
        if (cursor.moveToNext()) {
            entity = new HistoryEntity();
            entity.setH_id(cursor.getInt(0));
            entity.setH_name(cursor.getString(1));
            entity.setH_path(cursor.getString(2));
            entity.setH_url(cursor.getString(3));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return entity;
    }

    public synchronized HistoryEntity queryHistoryForURL(String url){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        HistoryEntity entity = null;
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                DatabaseConfig.HistoryTable.Columns.H_ID,
                DatabaseConfig.HistoryTable.Columns.H_NAME,
                DatabaseConfig.HistoryTable.Columns.H_PATH,
                DatabaseConfig.HistoryTable.Columns.H_URL,
        },DatabaseConfig.HistoryTable.Columns.H_URL+"=?",new String[]{url},null,null,null);
        if (cursor.moveToNext()) {
            entity = new HistoryEntity();
            entity.setH_id(cursor.getInt(0));
            entity.setH_name(cursor.getString(1));
            entity.setH_path(cursor.getString(2));
            entity.setH_url(cursor.getString(3));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return entity;
    }

    public synchronized HistoryEntity queryHistoryForHID(int h_id){
        SQLiteDatabase sqlite = mdb_DownLoad.getReadableDatabase();
        HistoryEntity entity = null;
        Cursor cursor = sqlite.query(DatabaseConfig.TaskTable.TABLE_NAME, new String[]{
                DatabaseConfig.HistoryTable.Columns.H_ID,
                DatabaseConfig.HistoryTable.Columns.H_NAME,
                DatabaseConfig.HistoryTable.Columns.H_PATH,
                DatabaseConfig.HistoryTable.Columns.H_URL,
        },DatabaseConfig.HistoryTable.Columns.H_ID+"=?",new String[]{String.valueOf(h_id)},null,null,null);
        if (cursor.moveToNext()) {
            entity = new HistoryEntity();
            entity.setH_id(cursor.getInt(0));
            entity.setH_name(cursor.getString(1));
            entity.setH_path(cursor.getString(2));
            entity.setH_url(cursor.getString(3));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        sqlite.close();
        return entity;
    }

    public synchronized void deleteHistory(int h_id){
        SQLiteDatabase sqlite = mdb_DownLoad.getWritableDatabase();
        sqlite.delete(DatabaseConfig.HistoryTable.TABLE_NAME, DatabaseConfig.HistoryTable.Columns.H_ID+"=?" , new String[]{String.valueOf(h_id)});
        sqlite.close();
    }


}
