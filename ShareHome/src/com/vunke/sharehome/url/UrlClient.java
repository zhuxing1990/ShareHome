package com.vunke.sharehome.url;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import android.support.annotation.Nullable;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;
import com.vunke.sharehome.utils.WorkLog;

/**
 *
 */
public class UrlClient {
	/**
	 * 正式服务器地址
	 */
	// public static final String HttpUrl =
	// "http://124.232.135.222:8080/ShareHome";// http://124.232.139.125 正式服务器
	/**
	 * 测试服务器地址
	 */
	public static final String HttpUrl 
	= "http://124.232.135.222:8080/ShareHome2";

	/**
	 * 本地测试服务器地址（朱燊）
	 */
	// public static final String HttpUrl
	// ="http://192.168.28.201:8080/ShareHome";
	/**
	 * 本地测试服务器地址2（陈庚）
	 */
//	 public static final String HttpUrl2
//	 ="http://192.168.28.226:80/ShareHome";
	/**
	 * 测试服务器地址2（陈庚）
	 */
	// public static final String HttpUrl2
	// ="http://134.175.18.53:8080/ShareHome2";
	/**
	 * 获取短信验证码接口
	 */
	public static final String getCode = "/sendMsg/getCode.do";
	/**
	 * 注册接口
	 */
	public static final String Register = "/sendMsg/signup.do";
	/**
	 * 修改密码接口
	 */
	public static final String UpdatePassword = "/sendMsg/updatePass.do";
	/**
	 * 短信验证码验证接口
	 */
	public static final String VaildateSmsCode = "/sendMsg/vaildateSmsCode.do";
	/**
	 * 登录接口
	 */
	public static final String SmsLogin = "/sendMsg/login.do";
	/**
	 * 登录记录接口
	 */
	public static final String LoginLog = "/loginLog.do";
	/**
	 * 新的朋友接口
	 */
	public static final String GetUserFriend = "/getUserFriend.do";
	/**
	 * 更多精彩 web页面
	 */
	public static final String MoreSurpris = "http://124.232.135.221:8080/portal/home.html";
	/**
	 * 分享的短链接
	 */
	public static final String ShareUrl = "http://url.cn/40fWu1d"; //http://t.cn/Rt4sTRX";
	/**
	 * 更新App接口
	 */
	public static final String ServerURL = "http://124.232.135.221:8080/shareHomePhon";

	/**
	 * 同步联系人接口
	 */
	public static final String UpdateContact = "/contact/asynContact.do";
	/**
	 * 删除联系人接口
	 */
	public static final String DeleteContact = "/contact/deleteContact.do";
	/**
	 * 获取所有联系人 (已废弃)接口
	 */
	public static final String GetAllContacts = "/contact/getContactAll.do";
	/**
	 * 获取所有联系人接口
	 */
	public static final String queryContactAll = "/contact/queryContactAll.do";
	/**
	 * 单个文件夹上传接口
	 */
	public static final String UploadFile = "/uploadFile.do";
	/**
	 * 多个文件夹上传接口
	 */
	public static final String MultipleFile = "/multipleFile.do";
	/**
	 * 从手机联系人中筛选想家用户(新版本)接口
	 */
	public static final String GetShareContacts = "/getShareContacts.do";
	/**
	 * Android 下载地址
	 */
	public static final String AppDownLoadURL = "http://124.232.135.225:8083/apkup/ShareHome_AND_V1.0.0.1-20160311.apk";
	/**
	 * IOS 下载地址
	 */
	public static final String IOSDownload = "http://mp.weixinbridge.com/mp/wapredirect?url=https://itunes.apple.com/app/id1130930231";
	/**
	 * 未接电话接口
	 */
	public static final String MissedCall = "/missedCall.do";
	/**
	 * 定向流量包获取短信验证码接口
	 */
	public static final String GetDnwSmsCode = "/getDnwSmsCode.do";
	/**
	 * 定向流量包订购接口
	 */
	public static final String DnwSmsOrder = "//dnwSmsOrder.do";
	/**
	 * 推荐人接口
	 */
	public static final String findReutestCode = "/findRequestCode.do";
	/**
	 * 推荐人接口
	 */
	public static final String fillRequestCode = "/fillRequestCode.do";
	// public static final String ServerURL =
	// "http://124.232.139.125:8080/shareHomePhon";
	/**
	 * 幸运转盘
	 */
	public static final String LuckyMoney = "/createLuckyMoney.do";
	/**
	 * 领取红包
	 */
	public static final String PayExchange = "/payExchange.do";
	/**
	 * 领取红包页面
	 */
	public static  String LuckyMoneyUrl = "";
//	public static final String LuckyMoneyUrl = "/luckymoney/home.jsp";
	/**
	 * 红包列表
	 */
	public static  String LuckyMoneyListUrl = "";
//	public static final String LuckyMoneyListUrl = "/luckymoney/listLucky.do";
	/**
	 * 活动信息
	 */
	public static final String ActivityInfo = "/activityInfo.do";
	public static String ActivityName =  "";
	public static void GetLuckyMoney() {
		try {

			OkHttpUtils
					.post(UrlClient.HttpUrl + UrlClient.ActivityInfo)
					.params("json",
							new JSONObject().put("activityId", "8")
									.toString())
					.execute(new StringCallback() {

						@Override
						public void onResponse(boolean isFromCache,
								String t, Request request,
								@Nullable Response response) {
							WorkLog.e("GetLuckyMoney", "获取数据" + t);
							try {
								JSONObject jsonobject = new JSONObject(t);
								if (jsonobject.has("code")) {
									int code = jsonobject.getInt("code");
									String message = jsonobject
											.getString("message");
									switch (code) {
									case 200:
										JSONObject getJsonUrl = new JSONObject(
												jsonobject
														.getString("locationUrl"));
										UrlClient.ActivityName = getJsonUrl
												.getString("activityName");
										UrlClient.LuckyMoneyUrl = getJsonUrl
												.getString("getLuckyUrl");
										UrlClient.LuckyMoneyListUrl = getJsonUrl
												.getString("listLuckyUrl");
										break;
									case 400:

										break;
									case 500:

										break;

									default:
										break;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onError(boolean isFromCache, Call call,
								Response response, Exception e) {
							WorkLog.e("GetLuckyMoney", "OnError");
						};
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
			
}
