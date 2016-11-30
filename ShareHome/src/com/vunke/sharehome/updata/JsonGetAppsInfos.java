package com.vunke.sharehome.updata;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonGetAppsInfos {
	/**
	 * 解析Json数据 获取全部应用信息
	 * 
	 * @param 应用信息查询接口链接
	 * @return 应用信息列表
	 * @throws Exception
	 */
	public static List<AllAppInfo> getALLAppInfos(String urlPath)
			throws Exception {
		// 初始化应用信息列表
		List<AllAppInfo> apps = new ArrayList<AllAppInfo>();
		// 从指定的url中获取字节数组
		byte[] data = readParse(urlPath);
		// 获取jason表头
//		JSONObject meta = new JSONObject(new String(data))
//				.getJSONObject("meta");
		// 获取Jason数据，对应应用信息部分，存放在jsonArray中
		JSONArray jsonArray = new JSONObject(new String(data))
				.getJSONArray("data");
		// 获取应用信息

		for (int i = 0; i < jsonArray.length(); i++) {
			// 生成一个应用信息类
			AllAppInfo app = new AllAppInfo();
			// 从jsonArray中获取应用信息
			JSONObject item = jsonArray.getJSONObject(i);
			app.setAppCount(item.getInt("appCount"));
			app.setAppDesc(item.getString("appDesc"));
			app.setAppIcon(item.getString("appIcon"));
			app.setAppId(item.getInt("appId"));
			app.setAppImg(item.getString("appImg"));
			app.setAppLevel(item.getInt("appLevel"));
			app.setAppName(item.getString("appName"));
			app.setAppShortName(item.getString("appShortName"));
			app.setAppSize(item.getString("appSize"));
			app.setAppUrl(item.getString("appUrl"));
			app.setAppVersion(item.getString("appVersion"));
			app.setUpdateTime(item.getString("updateTime"));
			app.setClassName(item.getString("className"));
			app.setClassId(item.getInt("classId"));
			app.setCreateTime(item.getString("createTime"));
			// 向应用信息列表中添加一个应用信息节点
			apps.add(app);
		}
		return apps;
	}
	// 获取AppTVStore更新信息
	public static List<AppTVStoreUpdateInfo> getAppUpdateInfo(String urlPath)
			throws Exception {
		List<AppTVStoreUpdateInfo> appUpdateInfos = new ArrayList<AppTVStoreUpdateInfo>();
		// 从指定的url中获取字节数组
		byte[] data = readParse(urlPath);
		// 获取jason表头
//		JSONObject meta = new JSONObject(new String(data))
//				.getJSONObject("meta");
		// 获取Jason数据，对应应用信息部分，存放在dataNode中
		JSONObject dataNode = new JSONObject(new String(data))
				.getJSONObject("data");
//		System.out.println(dataNode.toString());   打印出地址
		AppTVStoreUpdateInfo appUpdateInfo = new AppTVStoreUpdateInfo();
		appUpdateInfo.setCreateTime(dataNode.getString("createTime"));
		appUpdateInfo.setUpdateId(dataNode.getInt("updateId"));
		appUpdateInfo.setUpdateType(dataNode.getInt("updateType"));
		appUpdateInfo.setUpdateUrl(dataNode.getString("updateUrl"));
		appUpdateInfo.setVersion(dataNode.getInt("version"));
		appUpdateInfos.add(appUpdateInfo);
		return appUpdateInfos;
	}

	/**
	 * 从指定的url中获取字节数组
	 * 
	 * @param urlPath
	 * @return 字节数组
	 * @throws Exception
	 */

	public static byte[] readParse(String urlPath) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		URL url = new URL(urlPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		conn.setConnectTimeout(10*1000);
//		if (conn.getResponseCode()!=200) {
//			throw new RuntimeException("请求url失败");
//		}
		InputStream inStream = conn.getInputStream();
		while ((len = inStream.read(data)) != -1) {
			outStream.write(data, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}
}