package com.kxy.auth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Random;

import android.content.Context;

public class Config {
	/**
	 * webservice 正式地址ip
	 */
	public static final String BASE_WS_URL = "http://222.246.132.231:8298/";
	 public static final String BASE_WS_URL2 = "http://124.232.136.239:8080/tvlauncher/";
//	public static final String BASE_WS_URL2 = "http://192.168.28.226:8080/tvlauncher/";
//	public static final String BASE_WS_URL2 = "http://192.168.28.201:8080/tvlauncher/";
//	public static final String BASE_WS_URL2 = "http://192.168.0.114:8080/tvlauncher/";
	// public static final String BASE_WS_URL2 =
	// "http://192.168.43.6:8080/tvlauncher/";
	/**
	 *  获取 User_Token
	 */
	public static final String AUTH = "authenticationURL.do";
	/**
	 * 获取  User_Info
	 */
	public static final String UPLOAD_AUTH_INFO = "auth.do";
	/**
	 * 已弃用
	 */
	public static final String  EPG_HOME_AUTH  = "/epgHomeAuth.do";
	/**
	 * 获取 Start_Info
	 */
	public static final String  GetStartInfo = "/startInfo.do";

	/**
	 * notify 推送消息 测试服务器地址
	 */
	public static final String Notify_Url = "http://124.232.135.238:8090/adpush_server";
	/**
	 * 
	 * 获取 notify 推送消息
	 */
	public static final String Push = "/push.action";
	/**
	 * 用户返回首页后传输数据到服务端
	 */
	public static final String Behaviour = "/behaviour.action";
	/**
	 * webservice 外网测试ip
	 */
	// public static final String BASE_WS_URL =
	// "http://124.232.135.227:8297/services";
	/**
	 * webservice 内网测试ip
	 */
	// public static final String BASE_WS_URL =
	// "http://192.168.141.10:8297/services";

	public static final String IdsLoginQueryInterface = "IdsLoginQueryInterface";

	public static final String SOAP_URL = "/IdsLoginQueryInterface";

	/**
	 * 亚信联创管理平台 Asiainfo_Linkage SpId
	 */
	public static final String LinkageManage_SPID = "99999999";

	/**
	 * 应用管理平台 AppStore SpId
	 */
	public static final String AppStoreManage_SPID = "99999998";

	/**
	 * 一平台 one SpId
	 */
	public static final String OneManage_SPID = "90000001";
	/**
	 * 二平台 two SpId
	 */
	public static final String twoManage_SPID = "90000002";

	/**
	 * 综调系统 Fully Integrated System SpId 简称 FIS SpId
	 */
	public static final String FIS_Manage_SPID = "90000011";

}
