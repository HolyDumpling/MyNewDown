package com.hy.library_download.view;

import com.hy.library_download.view.base.DownloadBaseBean;

public class DownloadBean extends DownloadBaseBean {
    private String subTitle;

    public DownloadBean(String title, String url) {
        super(title, url);
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getSubTitle() {
        return subTitle;
    }
}
