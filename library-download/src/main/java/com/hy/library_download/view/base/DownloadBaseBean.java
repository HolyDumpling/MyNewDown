package com.hy.library_download.view.base;

import com.hy.library_download.db.TaskEntity;

public class DownloadBaseBean {
    private int id;
    private String task_name;
    private String url;
    private int status;
    private double[][] pos;


    public DownloadBaseBean(String title,String url) {
        this.task_name = title;
        this.url = url;
    }

    public void putTaskEntity(TaskEntity taskEntity){
        id = taskEntity.task_id;
        task_name = taskEntity.task_name;
        url = taskEntity.task_url;
        status = taskEntity.task_status;
    }

    public TaskEntity getTaskEntity(){
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.task_id = id;
        taskEntity.task_name = task_name;
        taskEntity.task_url = url;
        taskEntity.task_status = status;
        return taskEntity;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double[][] getPos() {
        return pos;
    }

    public void setPos(double[][] pos) {
        this.pos = pos;
    }
}
