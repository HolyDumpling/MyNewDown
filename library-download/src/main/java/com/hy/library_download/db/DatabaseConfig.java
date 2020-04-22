package com.hy.library_download.db;

public class DatabaseConfig {
    public final static class TaskTable{
        public static final String TABLE_NAME = "TaskTable";
        public static final class Columns {
            //任务ID
            public static final String TASK_ID = "task_id";
            //任务名称
            public static final String TASK_NAME ="task_name";
            //任务状态
            public static final String TASK_STATUS ="task_status";
            //任务链接
            public static final String TASK_URL ="task_url";
            //断点时间，用于确定上次断点后文件没有更改，可以断点续传
            public static final String TASK_LASTMODIFY ="task_lastModify";
            //任务总大小
            public static final String TASK_TOTAL ="task_total";
            //任务优先级（0是默认，数越大，优先级越低）
            public static final String TASK_PRIORITY ="task_priority";
        }
        //创建表
        public static final String CREATE_TABLE = "create table "+TABLE_NAME+"("
                //任务id
                + Columns.TASK_ID+" integer primary key autoincrement,"
                //任务名称
                + Columns.TASK_NAME+" text,"
                //任务状态
                + Columns.TASK_STATUS+" integer,"
                //任务链接
                + Columns.TASK_URL+" text,"
                //断点时间
                + Columns.TASK_LASTMODIFY+" text,"
                //任务总大小
                + Columns.TASK_TOTAL+" integer,"
                //任务优先级
                + Columns.TASK_PRIORITY+" integer)";

        /**
         * 删除表的SQL
         */
        static final String DROP_SQL = "drop table if exists " + TABLE_NAME;
    }
    public final static class SublineTable{
        public static final String TABLE_NAME = "SublineTable";
        public static final class Columns {
            //子线ID
            public static final String SL_ID = "sl_id";
            //子线ID
            public static final String TASK_ID = "task_id";
            //子线状态
            public static final String SL_STATUS ="sl_status";
            //子线名称
            public static final String SL_SAVE_PATH ="sl_save_path";
            //下载链接
            public static final String SL_URL ="sl_url";
            //起始位置
            public static final String SL_START_LABLE ="sl_start_lable";
            //结束位置
            public static final String SL_END_LABLE ="sl_end_lable";
            //已下载位置
            public static final String SL_DOWNED_LABLE ="sl_downed_lable";
        }
        //创建表
        public static final String CREATE_TABLE = "create table "+TABLE_NAME+"("
                //子线ID
                + Columns.SL_ID+" integer primary key autoincrement,"
                //任务ID
                + Columns.TASK_ID+" integer,"
                //子线状态
                + Columns.SL_STATUS+" integer,"
                //子线名称
                + Columns.SL_SAVE_PATH+" text,"
                //子线链接
                + Columns.SL_URL+" text,"
                //开始标记
                + Columns.SL_START_LABLE+" integer,"
                //结束标记
                + Columns.SL_END_LABLE+" integer,"
                //进度标记
                + Columns.SL_DOWNED_LABLE+" integer)";
        /**
         * 删除表的SQL
         */
        static final String DROP_SQL = "drop table if exists " + TABLE_NAME;
    }
    public final static class HistoryTable{
        public static final String TABLE_NAME = "HistoryTable";
        public static final class Columns {
            //历史ID
            public static final String H_ID = "h_id";
            //历史名称
            public static final String H_NAME ="h_name";
            //历史路径
            public static final String H_PATH ="h_path";
            //历史链接
            public static final String H_URL ="h_url";;
        }
        //创建表
        public static final String CREATE_TABLE = "create table "+TABLE_NAME+"("
                //历史ID
                + Columns.H_ID+" integer primary key autoincrement,"
                //历史名称
                + Columns.H_NAME+" text,"
                //历史路径
                + Columns.H_PATH+" text,"
                //历史链接
                + Columns.H_URL+" text)";
        /**
         * 删除表的SQL
         */
        static final String DROP_SQL = "drop table if exists " + TABLE_NAME;
    }
}
