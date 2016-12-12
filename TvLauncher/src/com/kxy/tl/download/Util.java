package com.kxy.tl.download;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 工具类.
 * @author cui_tao.
 */
public final class Util {
    /**
     * 存储单位.
     */
    private static final int STOREUNIT = 1024;

    /**
     * 时间毫秒单位.
     */
    private static final int TIMEMSUNIT = 1000;

    /**
     * 时间单位.
     */
    private static final int TIMEUNIT = 60;

    /**
     * 私有构造函数.
     */
    private Util() {
    }

    /**
     * 转化文件单位.
     * @param size 转化前大小(byte)
     * @return 转化后大小
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / STOREUNIT;
        if (kiloByte < 1) {
            return size + " Byte";
        }

        double megaByte = kiloByte / STOREUNIT;
        if (megaByte < 1) {
            BigDecimal result = new BigDecimal(Double.toString(kiloByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " KB";
        }

        double gigaByte = megaByte / STOREUNIT;
        if (gigaByte < 1) {
            BigDecimal result = new BigDecimal(Double.toString(megaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " MB";
        }

        double teraBytes = gigaByte / STOREUNIT;
        if (teraBytes < 1) {
            BigDecimal result = new BigDecimal(Double.toString(gigaByte));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " GB";
        }
        BigDecimal result = new BigDecimal(teraBytes);
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " TB";
    }

    /**
     * 转化时间单位.
     * @param time 转化前大小(MS) 
     * @return 转化后大小
     */
    public static String getFormatTime(long time) {
        double second = (double) time / TIMEMSUNIT;
        if (second < 1) {
            return time + " MS";
        }

        double minute = second / TIMEUNIT;
        if (minute < 1) {
            BigDecimal result = new BigDecimal(Double.toString(second));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " SEC";
        }

        double hour = minute / TIMEUNIT;
        if (hour < 1) {
            BigDecimal result = new BigDecimal(Double.toString(minute));
            return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " MIN";
        }

        BigDecimal result = new BigDecimal(Double.toString(hour));
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " H";
    }

    /**
     * 转化字符串.
     * @param source 转化前字符串
     * @param encoding 编码格式
     * @return 转化后字符串
     */
    public static String convertString(String source, String encoding) {
        try {
            byte[] data = source.getBytes("ISO8859-1");
            return new String(data, encoding);
        } catch (UnsupportedEncodingException ex) {
            return source;
        }
    }
    public static boolean isNetworkConnected(Context ct) {
		ConnectivityManager cManager = (ConnectivityManager) ct
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cManager != null) {
			NetworkInfo localNetworkInfo = cManager.getActiveNetworkInfo();
			if (localNetworkInfo != null)
				return localNetworkInfo.isConnected();
		}
		return false;
	}

	public static boolean isNetworkAvailable(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	  /**
     * 判断当前网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isNetConnected(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {

                NetworkInfo info = connectivity.getActiveNetworkInfo();

                if (info!=null) {
                	boolean istrue = false;
                	istrue= istrue?info.isConnected():info.isAvailable();
                	return istrue;
				}
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
