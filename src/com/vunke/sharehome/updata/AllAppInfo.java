package com.vunke.sharehome.updata;

import java.io.Serializable;
import java.util.Comparator;

//全部应用信息
public class AllAppInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private int appCount;
	private String appDesc;
	private String appIcon;
	private int appId;
	private String appImg;
	private float appLevel;
	private String appName;
	private String appShortName;
	private String appSize;
	private String appUrl;
	private String appVersion;
	private int classId;
	private String className;
	private String createTime;
	private String updateTime;

	public int getAppCount() {
		return appCount;
	}

	public void setAppCount(int appCount) {
		this.appCount = appCount;
	}

	public String getAppDesc() {
		return appDesc;
	}

	public void setAppDesc(String appDesc) {
		this.appDesc = appDesc;
	}

	public String getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(String string) {
		this.appIcon = string;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getAppImg() {
		return appImg;
	}

	public void setAppImg(String appImg) {
		this.appImg = appImg;
	}

	public float getAppLevel() {
		return appLevel;
	}

	public void setAppLevel(float appLevel) {
		this.appLevel = appLevel;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppShortName() {
		return appShortName;
	}

	public void setAppShortName(String appShortName) {
		this.appShortName = appShortName;
	}

	public String getAppSize() {
		return appSize;
	}

	public void setAppSize(String appSize) {
		this.appSize = appSize;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	// 测试用的
	public String getappString() {
		String ad = "appCount:" + appCount + "//appDesc:" + appDesc
				+ "//appIcon:" + appIcon + "//appId:" + appId + "//appImg:"
				+ appImg + "//appLevel:" + appLevel + "//appName:" + appName
				+ "//appShortName:" + appShortName + "//appSize:" + appSize
				+ "//appUrl:" + appUrl + "//appVersion:" + appVersion
				+ "//classId:" + classId + "//className:" + className
				+ "//createTime:" + createTime + "//updateTime:" + updateTime
				+ "#### /n";
		return ad;
	}

	@Override
	public String toString() {
		return "AllAppInfo{" +
				"appCount=" + appCount +
				", appDesc='" + appDesc + '\'' +
				", appIcon='" + appIcon + '\'' +
				", appId=" + appId +
				", appImg='" + appImg + '\'' +
				", appLevel=" + appLevel +
				", appName='" + appName + '\'' +
				", appShortName='" + appShortName + '\'' +
				", appSize='" + appSize + '\'' +
				", appUrl='" + appUrl + '\'' +
				", appVersion='" + appVersion + '\'' +
				", classId=" + classId +
				", className='" + className + '\'' +
				", createTime='" + createTime + '\'' +
				", updateTime='" + updateTime + '\'' +
				'}';
	}

	// 比较器 排序使用
	// 按照更新时间排序
	public static class AppInfoComparator implements Comparator<AllAppInfo> {
		@Override
		public int compare(AllAppInfo appInfo1, AllAppInfo appInfo2) {
			int result = appInfo2.getUpdateTime().compareTo(
					appInfo1.getUpdateTime());
			// int result = appInfo1.getAppShortName().compareTo(
			// appInfo2.getAppShortName());
			if (0 == result) {
				return appInfo1.getAppShortName().compareTo(
						appInfo2.getAppShortName());
			}
			return result;
		}

	}

	// 按照下载量排序
	public static class AppInfoComparator2 implements Comparator<AllAppInfo> {
		@Override
		public int compare(AllAppInfo appInfo1, AllAppInfo appInfo2) {
			int result = 0;
			if (appInfo2.appCount > appInfo1.appCount) {
				result = 1;
			} else if (appInfo2.appCount < appInfo1.appCount) {
				result = -1;
			} else {
				result = 0;
			}
			// int result = appInfo1.getAppShortName().compareTo(
			// appInfo2.getAppShortName());
			if (0 == result) {
				return appInfo1.getAppShortName().compareTo(
						appInfo2.getAppShortName());
			}
			return result;
		}

	}

	// 按照评分排序
	public static class AppInfoComparator3 implements Comparator<AllAppInfo> {
		@Override
		public int compare(AllAppInfo appInfo1, AllAppInfo appInfo2) {
			int result = 0;
			if (appInfo2.appLevel > appInfo1.appLevel) {
				result = 1;
			} else if (appInfo2.appLevel < appInfo1.appLevel) {
				result = -1;
			} else {
				result = 0;
			}
			// int result = appInfo1.getAppShortName().compareTo(
			// appInfo2.getAppShortName());
			if (0 == result) {
				return appInfo1.getAppShortName().compareTo(
						appInfo2.getAppShortName());
			}
			return result;
		}

	}
}
