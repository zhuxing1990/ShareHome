package com.kxy.tl.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.kxy.ti.util.Constants;
import com.kxy.ti.util.FileManager;
import com.kxy.ti.util.LogUtil;
import com.kxy.ti.util.SharedPreferencesUtil;

public class DownLoadService extends Service {

	private FTP ftp;
	private String HTTP_URL = "http://124.232.135.225:9290/kaiji/list.txt";
	private List<FTPFile> remoteFile;
	private String TAG = "DownLoadService";
	private String LOCAL_PATH = "";
	private String LOCAL_HTTP_PATH = "";
	private String LOCAL_PATH2 = "";
	private boolean isDownLoading = false;
	private final String KEY_FOR_DOWNLOAD_DATE = "key_for_download_date";
	private final String KEY_FOR_HTTP_TIME = "key_for_http_time";
	private long time;
	
	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.i("tv_launcher", "--service in --onStartCommand--");
		LogUtil.i("tv_launcher", "--isDownLoading--"+isDownLoading);
		LOCAL_PATH = DownLoadService.this.getApplicationContext().getFilesDir().getAbsolutePath()+ Constants.ADVERT_SAVE_PATH;
		LOCAL_HTTP_PATH = DownLoadService.this.getApplicationContext().getFilesDir().getAbsolutePath()+ Constants.ADVERT_HTTP_SAVE_PATH;
		LOCAL_PATH2 = DownLoadService.this.getApplicationContext().getFilesDir().getAbsolutePath()+ Constants.ADVERT_PALY_PATH;
		if(!isDownLoading){
			LogUtil.i("tv_launcher", "--start Thread--");
			new Thread(rb).start();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@SuppressLint("SimpleDateFormat")
	private void startDownLoad() {
		LogUtil.i("tv_launcher", "--startDownLoad--");
		Result result = null;
		isDownLoading = true;
		if(remoteFile == null || remoteFile.size() == 0){
			isDownLoading = false;
			return;
		}
		try {
			makeDir();
			// 下载
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			result = ftp.download(FTP.REMOTE_PATH, FTP.FolderName, LOCAL_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		isDownLoading = false;
		if (null != result && result.isSucceed()) {
			Log.e(TAG, "download ok...time:" + result.getTime() + " and size:"
					+ result.getResponse());
//			SharedPreferencesUtil.setLongValue(DownLoadService.this, KEY_FOR_DOWNLOAD_DATE, time);
			try {
				localFileDelete(new File(LOCAL_PATH2));
				copyDirectiory(LOCAL_PATH, LOCAL_PATH2);
				LogUtil.i("tv_launcher", "--copyFile OK--");
				SharedPreferencesUtil.setLongValue(DownLoadService.this, KEY_FOR_DOWNLOAD_DATE, time);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			LogUtil.e("tv_launcher", "download fail");
		}
	}

	private void initFtp() {
		LogUtil.i("tv_launcher", "--initFtp--");
		try {
			if (ftp != null) {
				// 关闭FTP服务
				ftp.closeConnect();
			}
			// 初始化FTP
			ftp = new FTP("121.42.151.157", "kzq", "kzq123456");
			// 打开FTP服务
			ftp.openConnect();
			// 初始化FTP列表
			remoteFile = new ArrayList<FTPFile>();
			// 加载FTP列表
			remoteFile = ftp.listFiles(FTP.REMOTE_PATH);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onDestroy() {
		super.onDestroy();
		// 关闭服务
		LogUtil.i("tv_launcher", "--ondestory--");
		try {
			ftp.closeConnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

	};

	public boolean needDownLoad() {
		boolean result = false;
		if(remoteFile == null || remoteFile.size()==0){
			return false;
		}
		if(!new File(LOCAL_PATH).exists() || !new File(LOCAL_PATH2).exists()){
			return true;
		}
        for (FTPFile ftpFile : remoteFile) {
            if (ftpFile.getName().equals(FTP.FolderName)) {
            	time = ftpFile.getTimestamp().getTimeInMillis();
            	if(time != SharedPreferencesUtil.getLongValue(DownLoadService.this, KEY_FOR_DOWNLOAD_DATE,-1L)){
            		result = true;
            	}
            }
        }
        LogUtil.i("tv_launcher", "--needDownLoad--"+result);
        return result;
	}

	public void localFileDelete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				file.delete();
				return;
			}
			for (File f : childFile) {
				localFileDelete(f);
			}
			file.delete();
		}
	}

	private void makeDir() {
		File file = new File(LOCAL_PATH);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		// 新建目标目录
		(new File(targetDir)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				LogUtil.e("tv_launcher", "准备复制的源文件夹"+dir1);
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				LogUtil.e("tv_launcher", "准备复制的目标文件夹"+dir2);
				copyDirectiory(dir1, dir2);
			}
		}
	}

	public static void copyFile(File sourceFile,File targetFile)   
			 throws IOException{  
			         // 新建文件输入流并对它进行缓冲   
			         FileInputStream input = new FileInputStream(sourceFile);  
			         BufferedInputStream inBuff=new BufferedInputStream(input);  
			   
			         // 新建文件输出流并对它进行缓冲   
			         FileOutputStream output = new FileOutputStream(targetFile);  
			         BufferedOutputStream outBuff=new BufferedOutputStream(output);  
			           
			         // 缓冲数组   
			         byte[] b = new byte[1024 * 5];  
			         int len;  
			         while ((len =inBuff.read(b)) != -1) {  
			             outBuff.write(b, 0, len);  
			         }  
			         // 刷新此缓冲的输出流   
			         outBuff.flush();  
			           
			         //关闭流   
			         inBuff.close();  
			         outBuff.close();  
			         output.close();  
			         input.close();  
			     }  

	Runnable rb = new Runnable() {
		
		@Override
		public void run() {
			List text = getHttpText();
			LogUtil.i("tv_launcher", "---run---"+text.toString());
			String updateTime = "";
			if(text != null && text.size()>0){
				updateTime = text.get(0).toString().trim();
				LogUtil.i("tv_launcher", "开机图片updateTime:"+updateTime);
			}
			String localTime = SharedPreferencesUtil.getStringValue(DownLoadService.this, KEY_FOR_HTTP_TIME, "");
				LogUtil.i("tv_launcher", "开机图片localTime:"+localTime);
			if(null != text && text.size()>0 && !updateTime.equals(localTime) ){
				LogUtil.i("tv_launcher", "updateTime not equals localTime,start updateing");
				localFileDelete(new File(LOCAL_PATH));
				for(int i=1;i<text.size();i++){
					getHttpFile(text.get(i).toString());
				}
				try {
					localFileDelete(new File(LOCAL_PATH2));
					copyDirectiory(LOCAL_PATH, LOCAL_PATH2);
					LogUtil.i("tv_launcher", "--copyFile OK--");
					SharedPreferencesUtil.setStringValue(DownLoadService.this, KEY_FOR_HTTP_TIME, updateTime);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}else{
				LogUtil.i("tv_launcher", "updateTime equals localTime,not update");
//				initFtp();
//				if (needDownLoad()) {
//					localFileDelete(new File(LOCAL_PATH));
//					startDownLoad();
//				}
			}
		}
	};
	
    /**  
     * @Desciption: 只能读取文本文件，读取mp3文件会出现内存溢出现象  
     */  
    private List getHttpText(){   
            String urlStr= HTTP_URL;   
            List<String> list = new ArrayList();
            try {   
                /*  
                 * 通过URL取得HttpURLConnection  
                 * 要网络连接成功，需在AndroidMainfest.xml中进行权限配置  
                 * <uses-permission android:name="android.permission.INTERNET" />  
                 */  
                URL url=new URL(urlStr);   
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();   
                //取得inputStream，并进行读取   
                InputStream input=conn.getInputStream();   
                BufferedReader in=new BufferedReader(new InputStreamReader(input));   
                String line=null;   
                int i=0;
                while((line=in.readLine())!=null){   
                	list.add(line);
                }   
                return list;
                   
            } catch (MalformedURLException e) {   
                e.printStackTrace();   
                return list;
            } catch (IOException e) {   
                e.printStackTrace();   
                return list;
            }   
    }  
    
    /**  
     * @Desciption: 读取任意文件，并将文件保存到手机SDCard  
     */  
    private void getHttpFile(String urlStr){   
    	LogUtil.i("tv_launcher", "----download----");
            String fileName= getFileName(urlStr);
            OutputStream output=null;   
            try {   
                URL url=new URL(urlStr);   
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();   
                //取得inputStream，并将流中的信息写入SDCard   
                   
                String SDCard=LOCAL_HTTP_PATH;
                String pathName=SDCard+"/"+fileName;//文件存储路径   
                LogUtil.i("tv_launcher", "down file directory:"+pathName);
                   
                File file=new File(pathName);   
                InputStream input=conn.getInputStream();   
                    String dir=SDCard;   
                    File dirf = new File(dir);
                    File pdir = new File(LOCAL_PATH);
                    if(!pdir.exists()){
                    	pdir.mkdir();
                    }
                    if(!dirf.exists()){
                    	dirf.mkdir();//新建文件夹   
                    }
                    file.createNewFile();//新建文件   
                    output=new FileOutputStream(file);   
                    //读取大文件   
                    int len=0;
                    byte[] buffer=new byte[1024];   
                    while((len=input.read(buffer))!=-1){   
                        output.write(buffer,0,len);   
                    }   
                    output.flush();   
            } catch (MalformedURLException e) {   
                e.printStackTrace();   
            } catch (IOException e) {   
                e.printStackTrace();   
            }finally{   
                try {   
                    if(null!= output){    
                	output.close();   
                    }
                    } catch (IOException e) {   
                        e.printStackTrace();   
                    }   
            }   
        } 
    
    private String getFileName(String url){
    	String fileName = "";
    	fileName = url.substring(url.lastIndexOf("/")+1);
    	return fileName; 
    }
           
}
