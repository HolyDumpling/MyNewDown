package com.hy.library_download.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };
    protected static MessageDigest messagedigest = null;
    static{
        try{
            messagedigest = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException nsaex){
            nsaex.printStackTrace();
        }
    }

    //生成文件的md5码
    public static String getFileMD5String(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        FileChannel ch = in.getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        messagedigest.update(byteBuffer);
        return bufferToHex(messagedigest.digest());
    }
    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }
    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }
    /**
     * 文件工具类函数
     * 作者：Android技术分享
     * 链接：https://www.jianshu.com/p/8dc08476261a
     * 来源：简书
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     * */
    /**
     * 需求：
     * -- 可删除单个文件，也可删除整个目录，
     * -- 先判断File类型是文件还是目录类型，
     * -- 如果是文件，则直接删除
     * -- 如果是目录，则获得目录信息，递归删除文件及文件夹
     * 方法传参：目标路径（文件或目录路径）
     **/
    public static void removeFile(String filePath) {
        if(filePath == null || filePath.length() == 0){
            return;
        }
        try {
            File file = new File(filePath);
            if(file.exists()){
                removeFile(file);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void removeFile(File file){
        //如果是文件直接删除
        if(file.isFile()){
            file.delete();
            return;
        }
        //如果是目录，递归判断，如果是空目录，直接删除，如果是文件，遍历删除
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                removeFile(f);
            }
            file.delete();
        }
    }

    /**
     * 创建文件
     * 需求：
     * -- 在指定目录创建文件，
     * -- 先判断目录是否存在，如果不存在，则递归创建，
     * -- 再判断文件是否存在，如果不存在，则创建
     * -- 最后，返回文件对象
     * 方法传参：目标路径、文件名称*/
    public static File newFile(String filePath, String fileName){
        if(filePath == null || filePath.length() == 0
                || fileName == null || fileName.length() == 0){
            return null;
        }
        try {
            //判断目录是否存在，如果不存在，递归创建目录
            File dir = new File(filePath);
            if(!dir.exists()){
                dir.mkdirs();
            }

            //组织文件路径
            StringBuilder sbFile = new StringBuilder(filePath);
            if(!filePath.endsWith("/")){
                sbFile.append("/");
            }
            sbFile.append(fileName);

            //创建文件并返回文件对象
            File file = new File(sbFile.toString());
            if(!file.exists()){
                file.createNewFile();
            }
            return file;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }



    /**
     * 获得文件或目录大小（size）
     * 需求：
     * -- 可获得单个文件大小，也可获得目录大小，
     * -- 先判断File类型是文件还是目录类型，
     * -- 如果是文件，则直接获取大小并返回
     * -- 如果是目录，递归获得各文件大小累加，然后返回
     * 方法传参：目标路径（文件或目录路径）*/
    public static float size = 0;
    public static float getFileSize(String filePath) {
        if(filePath == null || filePath.length() == 0){
            return 0;
        }
        try {
            File file = new File(filePath);
            if(file.exists()){
                size = 0;
                return getSize(file);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 0;
    }

    public static float getSize(File file) {
        try {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                for (File f : children) {
                    size += getSize(f);
                }
                return size;
            }
            //如果是文件则直接返回其大小
            else {
                return (float) file.length();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return size;
    }

    /**
     * 拷贝文件
     * 需求：
     * -- 将文件拷贝到指定目录
     * -- 如果目录不存在，则递归创建
     * -- 拷贝文件
     * 方法传参：文件路径、拷贝目标路径*/
    public static void copyFile(String filePath, String newDirPath) {
        if(filePath == null || filePath.length() == 0){
            return;
        }
        try {
            File file = new File(filePath);
            if(!file.exists()){
                return;
            }
            //判断目录是否存在，如果不存在，则创建
            File newDir = new File(newDirPath);
            if(!newDir.exists()){
                newDir.mkdirs();
            }
            //创建目标文件
            File newFile = newFile(newDirPath, file.getName());
            InputStream is = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[4096];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拷贝目录
     * 需求：
     * -- 将整个目录拷贝到指定目录
     * -- 如果目录不存在，则递归创建
     * -- 递归拷贝文件
     * 方法传参：源目录路径、拷贝目标路径*/
    public static void copyDir(String dirPath, String newDirPath) {
        if(dirPath == null || dirPath.length() == 0
                || newDirPath==null || newDirPath.length() == 0){
            return;
        }
        try {
            File file = new File(dirPath);
            if(!file.exists() && !file.isDirectory()){
                return;
            }
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                return;
            }
            File newFile = new File(newDirPath);
            newFile.mkdirs();
            for (File fileTemp : childFile) {
                if(fileTemp.isDirectory()){
                    copyDir(fileTemp.getPath(), newDirPath + "/" + fileTemp.getName());
                }else {
                    copyFile(fileTemp.getPath(), newDirPath);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 剪切文件
     * 需求：
     * -- 将文件剪切到指定目录
     * -- 如果目录不存在，则递归创建
     * -- 拷贝文件，删除源文件
     * 方法传参：源文件路径、拷贝目标路径*/
    public static void moveFile(String filePath, String newDirPath) {
        if(filePath == null || filePath.length() == 0
                || newDirPath==null || newDirPath.length() == 0){
            return;
        }
        try {
            //拷贝文件
            copyFile(filePath, newDirPath);
            //删除原文件
            removeFile(filePath);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 剪切目录
     * 需求：
     * -- 将目录剪切到指定目录
     * -- 如果目录不存在，则递归创建
     * -- 递归拷贝文件，删除源目录
     * 方法传参：源目录路径、拷贝目标路径*/
    public static void moveDir(String dirPath, String newDirPath) {
        if(dirPath == null || dirPath.length() == 0
                || newDirPath==null || newDirPath.length() == 0){
            return;
        }
        try {
            //拷贝目录
            copyDir(dirPath, newDirPath);
            //删除目录
            removeFile(dirPath);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
