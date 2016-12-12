package com.kxy.tl.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.kxy.ti.util.LogUtil;

/**
 * FTP封装类.
 *  
 * @author cui_tao
 */
public class FTP {
    /**
     * 服务器名.
     */
    private String hostName;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;

    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    /**
     * FTP列表.
     */
    private List<FTPFile> list;

    /**
     * FTP目录.
     */
    public static final String REMOTE_PATH = "/kzq/pub/";
    
    public static final String FolderName = "ftptest";
    
    /**
     * FTP当前目录.
     */
    private String currentPath = "";

    /**
     * 统计流量.
     */
    private double response;

    /**
     * 构造函数.
     * @param host hostName 服务器名
     * @param user userName 用户名
     * @param pass password 密码
     */
    public FTP(String host, String user, String pass) {
        this.hostName = host;
        this.userName = user;
        this.password = pass;
        this.ftpClient = new FTPClient();
        this.list = new ArrayList<FTPFile>();
    }

    /**
     * 打开FTP服务.
     * @throws IOException 
     */
    public void openConnect() throws IOException {
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器
        ftpClient.connect(hostName);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        }
        // 登录到服务器
        ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            System.out.println("login");
        }
    }

    /**
     * 关闭FTP服务.
     * @throws IOException 
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            // 登出FTP
            ftpClient.logout();
            // 断开连接
            ftpClient.disconnect();
            System.out.println("logout");
        }
    }

    /**
     * 列出FTP下所有文件.
     * @param remotePath 服务器目录
     * @return FTPFile集合
     * @throws IOException 
     */
    public List<FTPFile> listFiles(String remotePath) throws IOException {
        // 获取文件
        FTPFile[] files = ftpClient.listFiles(remotePath);
        // 遍历并且添加到集合
        for (FTPFile file : files) {
            list.add(file);
        }
        return list;
    }

    /**
     * 下载.
     * @param remotePath FTP目录
     * @param fileName 文件名
     * @param localPath 本地目录
     * @return Result
     * @throws IOException 
     */
    public Result download(String remotePath, String fileName, String localPath) throws IOException {
        boolean flag = true;
        Result result = null;
        // 初始化FTP当前目录
        currentPath = remotePath;
        LogUtil.i("tv_launcher", "currentPath--"+currentPath);
        // 初始化当前流量
        response = 0;
        // 更改FTP目录
        ftpClient.changeWorkingDirectory(remotePath);
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        LogUtil.i("tv_launcher", "ftpFiles--"+ftpFiles.toString());
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {
            // 找到需要下载的文件
            if (ftpFile.getName().equals(fileName)) {
                System.out.println("download...");
                // 创建本地目录
                File file = new File(localPath + "/" + fileName);
                LogUtil.i("tv_launcher", "file--"+file.getAbsolutePath());
                // 下载前时间
                Date startTime = new Date();
                if (ftpFile.isDirectory()) {
                    // 下载多个文件
                    flag = downloadMany(file);
                } else {
                    // 下载当个文件
                    flag = downloadSingle(file, ftpFile);
                }
                // 下载完时间
                Date endTime = new Date();
                // 返回值
                result = new Result(flag, Util.getFormatTime(endTime.getTime() - startTime.getTime()), Util.getFormatSize(response));
            }
        }
        return result;
    }

    /**
     * 下载单个文件.
     * @param localFile 本地目录
     * @param ftpFile FTP目录
     * @return true下载成功, false下载失败
     * @throws IOException 
     */
    private boolean downloadSingle(File localFile, FTPFile ftpFile) throws IOException {
        boolean flag = true;
        // 创建输出流
        OutputStream outputStream = new FileOutputStream(localFile);
        // 统计流量
        response += ftpFile.getSize();
        // 下载单个文件
        flag = ftpClient.retrieveFile(localFile.getName(), outputStream);
        // 关闭文件流
        outputStream.close();
        return flag;
    }

    /**
     * 下载多个文件.
     * @param localFile 本地目录
     * @return true下载成功, false下载失败
     * @throws IOException 
     */
    private boolean downloadMany(File localFile) throws IOException {
        boolean flag = true;
        // FTP当前目录
        if (!currentPath.equals(REMOTE_PATH)) {
            currentPath = currentPath + REMOTE_PATH + localFile.getName();
            LogUtil.i("tv_launcher", "if currentPath--"+currentPath);
        } else {
            currentPath = currentPath + localFile.getName();
            LogUtil.i("tv_launcher", "else currentPath--"+currentPath);
        }
        // 创建本地文件夹
        localFile.mkdir();
        // 更改FTP当前目录
        ftpClient.changeWorkingDirectory(currentPath);
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {
            // 创建文件
            File file = new File(localFile.getPath() + "/" + ftpFile.getName());
            if (ftpFile.isDirectory()) {
                // 下载多个文件
                flag = downloadMany(file);
            } else {
                // 下载单个文件
                flag = downloadSingle(file, ftpFile);
            }
        }
        return flag;
    }

}