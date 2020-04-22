package com.hy.mynewdown;

import androidx.annotation.Nullable;

import com.hy.library_download.view.DownloadBean;

import java.util.ArrayList;

public class ResourceLibrary {
    private  ArrayList<DownloadBean> resourceLibrary = new ArrayList<>();

    public ResourceLibrary(){
        resourceLibrary.add(new DownloadBean("qq2020.exe","http://dl.static.iqiyi.com/hz/IQIYIsetup_z43.exe"));
        resourceLibrary.add(new DownloadBean("360.exe","https://dl.360safe.com/setup.exe"));
        resourceLibrary.add(new DownloadBean("360se.exe","https://dl.360safe.com/se/360se_setup.exe"));
        resourceLibrary.add(new DownloadBean("360sd.exe","https://dl.360safe.com/360sd/360sd_x64_std_5.0.0.8160B.exe"));
        resourceLibrary.add(new DownloadBean("360zip.exe","https://dl.360safe.com/360zip_setup_4.0.0.1210.exe"));
        resourceLibrary.add(new DownloadBean("360CleanMasterPC.exe","http://down.360safe.com/360CleanMasterPC/Setup_360CleanMaster.exe"));
        resourceLibrary.add(new DownloadBean("360se10.exe","http://down.360safe.com/se/360se10.0.2404.0_wsonlineworkpr3.exe"));

        /*
        resourceLibrary.add(new BookBean("唐砖全文","http://down.bxwxorg.com/all/31/90703/%E5%94%90%E7%A0%96%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("刺青全文","http://down.bxwxorg.com/all/86066/468238/%E5%88%BA%E9%9D%92%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("捡漏全文","http://down.bxwxorg.com/all/12803/571150/%E6%8D%A1%E6%BC%8F%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("小甜蜜全文","http://down.bxwxorg.com/all/87617/476015/%E5%B0%8F%E7%94%9C%E8%9C%9C%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("魂武双修全文","http://down.bxwxorg.com/all/211/6628/%E9%AD%82%E6%AD%A6%E5%8F%8C%E4%BF%AE%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("完美世界全文","http://down.bxwxorg.com/all/11/73191/%E5%AE%8C%E7%BE%8E%E4%B8%96%E7%95%8C%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("飞剑问道全文","http://down.bxwxorg.com/all/45/201804/%E9%A3%9E%E5%89%91%E9%97%AE%E9%81%93%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("诡秘之主全文","http://down.bxwxorg.com/all/899/205875/%E8%AF%A1%E7%A7%98%E4%B9%8B%E4%B8%BB%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("三千鸦杀全文","http://down.bxwxorg.com/all/4883/349315/%E4%B8%89%E5%8D%83%E9%B8%A6%E6%9D%80%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("全职法师全文","http://down.bxwxorg.com/all/1936/119589/%E5%85%A8%E8%81%8C%E6%B3%95%E5%B8%88%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("职业替身全文","http://down.bxwxorg.com/all/39355/374034/%E8%81%8C%E4%B8%9A%E6%9B%BF%E8%BA%AB%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("偏执宠爱全文","http://down.bxwxorg.com/all/87461/483761/%E5%81%8F%E6%89%A7%E5%AE%A0%E7%88%B1%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("农门科举全文","http://down.bxwxorg.com/all/88297/464336/%E5%86%9C%E9%97%A8%E7%A7%91%E4%B8%BE%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("芙蓉帐暖全文","http://down.bxwxorg.com/all/91850/464737/%E8%8A%99%E8%93%89%E5%B8%90%E6%9A%96%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("太古龙象诀全文","http://down.bxwxorg.com/all/4211/174385/%E5%A4%AA%E5%8F%A4%E9%BE%99%E8%B1%A1%E8%AF%80%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("太子宠妃日常全文","http://down.bxwxorg.com/all/89069/482441/%E5%A4%AA%E5%AD%90%E5%AE%A0%E5%A6%83%E6%97%A5%E5%B8%B8%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("女主都和男二HE全文","http://down.bxwxorg.com/all/85865/479356/%E5%A5%B3%E4%B8%BB%E9%83%BD%E5%92%8C%E7%94%B7%E4%BA%8CHE%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("恰似寒光遇骄阳全文","http://down.bxwxorg.com/all/12897/200945/%E6%81%B0%E4%BC%BC%E5%AF%92%E5%85%89%E9%81%87%E9%AA%84%E9%98%B3%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("愤怒值爆表[快穿]全文","http://down.bxwxorg.com/all/86661/447714/%E6%84%A4%E6%80%92%E5%80%BC%E7%88%86%E8%A1%A8[%E5%BF%AB%E7%A9%BF]%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("总有偏执狂想独占我全文","http://down.bxwxorg.com/all/86924/689349/%E6%80%BB%E6%9C%89%E5%81%8F%E6%89%A7%E7%8B%82%E6%83%B3%E7%8B%AC%E5%8D%A0%E6%88%91%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
        resourceLibrary.add(new BookBean("侯门弃女：妖孽丞相赖上门全文","http://down.bxwxorg.com/all/34604/186710/%E4%BE%AF%E9%97%A8%E5%BC%83%E5%A5%B3%EF%BC%9A%E5%A6%96%E5%AD%BD%E4%B8%9E%E7%9B%B8%E8%B5%96%E4%B8%8A%E9%97%A8%E5%85%A8%E6%96%87@www.bxwxorg.com.txt"));
    */
    }




    public  ArrayList<DownloadBean> getResourceLibrary(){
        return resourceLibrary;
    }
}
