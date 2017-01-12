package com.vunke.sharehome.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 文件工具类
 * 
 * @author zhuxi
 * 
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
@SuppressLint("NewApi")
public class FileUtils {

	public static String SDPATH = Environment.getExternalStorageDirectory()
			+ "/DCIM/Camera/";

	// public static void saveBitmap(Bitmap bm, String picName) {
	// WorkLog.a("-----------------------------");
	// try {
	// if (!isFileExist("")) {
	// WorkLog.a("创建文件");
	// File tempf = createSDDir("");
	// }
	// File f = new File(SDPATH, picName + ".JPEG");
	// if (f.exists()) {
	// f.delete();
	// }
	// FileOutputStream out = new FileOutputStream(f);
	// bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
	// out.flush();
	// out.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public static File createSDDir(String dirName) throws IOException {
		File dir = new File(SDPATH + dirName);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			WorkLog.a("createSDDir:" + dir.getAbsolutePath());
			WorkLog.a("createSDDir:" + dir.mkdir());
		}
		return dir;
	}

	public String getFromAssets(String fileName, Context context) {
		try {
			InputStreamReader inputReader = new InputStreamReader(context
					.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String Result = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// public static boolean isFileExist(String fileName) {
	// File file = new File(SDPATH + fileName);
	// file.isFile();
	// WorkLog.a(file.exists());
	// return file.exists();
	// }
	public static boolean isFileExist(String fileName) {
		File file = new File(fileName);
		file.isFile();
		Log.e("tag", "文件" + file.exists());
		return file.exists();
	}

	public static void delFile(String fileName) {
		File file = new File(SDPATH + fileName);
		if (file.isFile()) {
			file.delete();
		}
		file.exists();
	}

	public static void deleteDir() {
		File dir = new File(SDPATH);
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete();
			else if (file.isDirectory())
				deleteDir();
		}
		dir.delete();
	}

	/**
	 * 删除文件或文件夹
	 * 
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isFile()) {
			final File to = new File(file.getAbsolutePath()
					+ System.currentTimeMillis());
			file.renameTo(to);
			to.delete();
		} else {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File innerFile : files) {
					deleteFile(innerFile);
				}
			}
			final File to = new File(file.getAbsolutePath()
					+ System.currentTimeMillis());
			file.renameTo(to);
			to.delete();
		}
	}

	public static boolean fileIsExists(String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {

			return false;
		}
		return true;
	}

	/**
	 * @param context
	 *            上下文
	 * @param uri
	 *            文件uri
	 * @return String 文件路径 获取文件路径
	 */
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	/**
	 * @param file
	 * @return
	 */
	public static Collection<File> getFileListOnSys(File file) {
		// 从根目录开始扫描

		Log.i("tag", file.getPath());

		Collection<File> fileList = new ArrayList<>();

		getFileList(file, fileList);

		return fileList;
	}

	/**
	 * @param path
	 * @param fileList
	 *            注意的是并不是所有的文件夹都可以进行读取的，权限问题
	 */
	private static void getFileList(File path, Collection<File> fileList) {
		// 如果是文件夹的话
		if (path.isDirectory()) {
			// 返回文件夹中有的数据
			File[] files = path.listFiles();
			// 先判断下有没有权限，如果没有权限的话，就不执行了
			if (null == files)
				return;

			for (int i = 0; i < files.length; i++) {
				getFileList(files[i], fileList);
			}
		}
		// 如果是文件的话直接加入
		else {
			Log.e("tag", path.getAbsolutePath());
			// 进行文件的处理
			String filePath = path.getAbsolutePath();
			// 文件名
			String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
			// 添加
			fileList.add(path);
		}
	}

	/**
	 * 获取sd卡剩余大小 (MB)
	 * 
	 * @return
	 */
	public static long getSDFreeSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// 获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		// 空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		// 返回SD卡空闲大小
		// return freeBlocks * blockSize; //单位Byte
		// return (freeBlocks * blockSize)/1024; //单位KB
		WorkLog.i("FileUtils", "当前内存空间:" + (freeBlocks * blockSize) / 1024
				/ 1024);
		return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
	}

	public static File[] getVideoFile(File file) {
		if (!file.exists()) {
			WorkLog.i("FileUtils", "文件不存在");
			return null;
		} else {
			File[] files = file.listFiles();
			if (files == null && files.length == 0) {
				WorkLog.i("FileUtils", "文件夹无任何文件");
				return null;
			} else {
				return files;
			}
		}
	}
}