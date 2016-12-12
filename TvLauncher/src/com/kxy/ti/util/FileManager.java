package com.kxy.ti.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class FileManager {
	private static FileManager mgr;
	private static Object mLock = new Object();
	public static int FILE_TYPE_PIC = 1;
	public static int FILE_TYPE_VIDEO = 2;

	public static FileManager instance() {
		synchronized (mLock) {
			if (mgr == null) {
				mgr = new FileManager();
			}
			return mgr;
		}
	}


	public static String isSdcard(Context aty) {
//		File sdcardDir = null;
//		boolean isSDExist = Environment.getExternalStorageState().equals(
//				Environment.MEDIA_MOUNTED);
//		if (isSDExist) {
//			// 如果存在SDcard 就找到跟目录
//			sdcardDir = Environment.getExternalStorageDirectory();
//			return sdcardDir.toString()+Constants.ADVERT_READ_PATH;
//		} else {
		LogUtil.i("tv_launcher", "--path--"+aty.getApplicationContext().getFilesDir().getAbsolutePath());
			return aty.getApplicationContext().getFilesDir().getAbsolutePath()+Constants.ADVERT_READ_PATH;
//		}
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("DefaultLocale")
	public static List<Drawable> getpicsPath(String sdpath,Resources res) {
		List<Drawable> drawableList = new ArrayList<Drawable>();
		// 打开SD卡目录
		File file = new File(sdpath);
		// 获取SD卡目录列表
		File[] files = file.listFiles();
		if(null == files){
			return drawableList;
		}

	    List <String>fileList = new ArrayList<String>();
		for (int z = 0; z < files.length; z++) {
			File f = files[z];
			if (f.isFile()) {
				String filename = f.getName();
				int idx = filename.lastIndexOf(".");
				if (idx <= 0) {
					return null;
				}
				String suffix = filename.substring(idx + 1, filename.length());
				if (suffix.toLowerCase().equals("jpg")
						|| suffix.toLowerCase().equals("jpeg")
						|| suffix.toLowerCase().equals("png")) {
					fileList.add(filename);
				}
			} else {
				Log.i("laucher", "not file");
			}
		}
	    Collections.sort(fileList, new Comparator() {
	        @Override
	        public int compare(Object o1, Object o2) {
	        	String first = ((String)o1).toString();
	        	String second = ((String)o2).toString();
	        	int a=0;
	        	int b=0;
	        	try{
		        	a = Integer.parseInt(first.substring(first.lastIndexOf("_")+1, first.lastIndexOf(".")));
		        	b = Integer.parseInt(second.substring(second.lastIndexOf("_")+1, second.lastIndexOf(".")));
	        	}catch(Exception e){
	        		LogUtil.i("tv_launcher", "------exception----");
	        		return 1;
	        	}
	          return ((Integer)a).compareTo((Integer)b);
	        }
	      });
	    LogUtil.i("tv_launcher", "---"+fileList.toString());
	    for(int j=0;j<fileList.size();j++){
			Bitmap bm = BitmapFactory.decodeFile(sdpath + "/"+fileList.get(j));
			Drawable drawable =new BitmapDrawable(res,bm);
			drawableList.add(drawable);
	    }
		return drawableList;

	}
	
	@SuppressWarnings("unchecked")
	public static List<Drawable> getPicFromAsset(Context ct,Resources res){
		List<Drawable> drawableList = new ArrayList<Drawable>();
		try {
			String[] list = ct.getAssets().list("");
		    List <String>fileList = new ArrayList<String>();

			for (String file : list) {
				if (file == null || "".equalsIgnoreCase(file))
					continue;
				if (file.endsWith(".jpg") || file.endsWith(".jpeg")
						|| file.endsWith(".png")) {
					fileList.add(file);
					
				}
			}
			
			 Collections.sort(fileList, new Comparator() {
			        @Override
			        public int compare(Object o1, Object o2) {
			        	String first = ((String)o1).toString();
			        	String second = ((String)o2).toString();
			        	int a=0;
			        	int b=0;
			        	try{
				        	a = Integer.parseInt(first.substring(first.lastIndexOf("_")+1, first.lastIndexOf(".")));
				        	b = Integer.parseInt(second.substring(second.lastIndexOf("_")+1, second.lastIndexOf(".")));
			        	}catch(Exception e){
			        		LogUtil.i("tv_launcher", "------exception----");
			        		return 1;
			        	}
			          return ((Integer)a).compareTo((Integer)b);
			        }
			      });
			    LogUtil.i("tv_launcher", "---"+fileList.toString());
			    for(int j=0;j<fileList.size();j++){
					Bitmap bm = BitmapFactory.decodeStream(ct.getAssets().open(fileList.get(j)));
					Drawable drawable =new BitmapDrawable(res,bm);
					drawableList.add(drawable);
			    }

		} catch (Exception e) {
			 e.printStackTrace();
		}

		return drawableList;
	}

	@SuppressLint("DefaultLocale")
	public static String VideoPath(String sdpath) {
		// 打开SD卡目录
		File file = new File(sdpath);
		// 获取SD卡目录列表
		File[] files = file.listFiles();
		if(null == files){
			return null;
		}
		for (int z = 0; z < files.length; z++) {
			File f = files[z];
			if (f.isFile()) {
				String filename = f.getName();
				int idx = filename.lastIndexOf(".");
				if (idx <= 0) {
					return null;
				}
				String suffix = filename.substring(idx + 1, filename.length());
				if (suffix.toLowerCase().equals("3gp")
						|| suffix.toLowerCase().equals("mp4")) {
					return sdpath + "/"+filename;
				}
			} else {
				Log.i("laucher", "not file");
			}
		}
		return null;
	}

	@SuppressLint("DefaultLocale")
	public static int getFileType(String sdpath) {
		// 打开SD卡目录
		File file = new File(sdpath);
		// 获取SD卡目录列表
		File[] files = file.listFiles();
		
		if(null == files){
			return 0;
		}
		
		for (int z = 0; z < files.length; z++) {
			File f = files[z];
			if (f.isFile()) {
				String filename = f.getName();
				int idx = filename.lastIndexOf(".");
				if (idx <= 0) {
					return 0;
				}
				String suffix = filename.substring(idx + 1, filename.length());
				if (suffix.toLowerCase().equals("jpg")
						|| suffix.toLowerCase().equals("jpeg")
						|| suffix.toLowerCase().equals("png")) {
					return FILE_TYPE_PIC;
				} else if (suffix.toLowerCase().equals("3gp")
						|| suffix.toLowerCase().equals("mp4")) {
					return FILE_TYPE_VIDEO;
				}
			} else {
				Log.i("laucher", "not file");
			}
		}
		return 0;
	}
}
