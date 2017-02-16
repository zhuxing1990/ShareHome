package com.vunke.sharehome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public final class Config {
	private Config() {

	}
	/**App修改时间**/
	public static final String appMotifyTime = "2016-12-01";
	/**欢迎界面的SharedPreferences的键名**/
	public static final String WELCOME = "welcome";
	/**引导页的SharedPreferences的键名**/
	public static final String IS_First = "is_first";
	/**首页的SharedPreferences的键名**/
	public static final String HOME = "home";
	/**获取头像SharedPreferences的键名**/
	public static final String HasPhoto = "hasPhoto";
	/**获取手机看家SharedPreferences的键名**/
	public static final String LookHome = "lookhome";
	/**获取用户帐号SharedPreferences的键名**/
	public static final String UserName = "UserName";
	/**首页的温馨提示(已弃用)**/
	public static final String IS_FIRST_DIALOG = "is_first_dialog";
	/**为账号号码前加密**/
	public static final String ACCOUNT_BEFORE = "+8611831726";
	/**为电话号码前加密**/
	public static final String CALL_BEFORE = "11831726"; 
	/**DM IP地址**/
	public static final String SIP = "10.255.31.235";
	/**DM端口**/
	public static final String SPORT = "443";
	/**数据库表名**/
	public static final String DATABASE_TABLE_NAME = "whiteContanct";
	/**数据库表的用户名**/
	public static final String TABLE_COLUMN_USERNAME = "username";
	/**数据库表的移动手机**/
	public static final String TABLE_COLUMN_MOBLIE = "moblie";
	/**数据库表的日期**/
	public static final String TABLE_COLUMN_DATE = "date";
	/**SharedPreferences  存储用户数据键名**/
	public static final String SP_NAME = "config";// 
	/**SharedPreferences 密码键名**/
	public static final String LOGIN_PASSWORD = "login_password";
	/**SharedPreferences 账号键名**/
	public static final String LOGIN_USER_NAME = "login_username";
	/**SharedPreferences 获取是否有用户名的键名**/														
	public static final String HasUserName = "hasUserName";
	/**SharedPreferences 手机看家的键名**/
	public static final String LOOKHOME = "lookhome";
	/**SharedPreferences 登录状态的键名**/
	public static final String RELOGIN = "relogin";
	/**暂不更新**/
	public static final String UPDATE_TOMORROW = "update_tomorrow";
	/**网络连接状态**/
	public static int net_connect_true = 0;
	/**登录次数**/
	public static int login_count = 0;
	
	/**默认的值**/
	public static long defaultValue = 0;
	
	public static Intent intent = null;// 意图

	public static final String NINE = "9";
	
	public static final String EIGHT = "8";
	/**上传联系人**/
	public static final String Upload_Contact = "upload_contact";
	/**新的朋友**/
	public static final String New_Friends = "new_friends";
	/** RxBus 更新联系人 **/
	public static final int Update_Contact = 7426248;
	/** RxBus 更新通话日志 **/
	public static final int Update_CallLog = 2255564;
	/** RxBus 更新登录 **/
	public static final int Update_Login = 336458;
	/** RxBus 更新头像 **/
	public static final int Update_Icon = 8689464;
	/**设置通话日志**/
	public static final int SetCallLog = 1221;
	/**设置搜索联系人**/
	public static final int SearchContact = 1111;
	/**设置联系人**/
	public static final int SetContact = 1;
	/**设置搜索想家联系人**/
	public static final int SearchShareHomeContact = 2;
	/**搜索本地联系人**/
	public static final int SearchContactApi1 = 0001;
	/**搜索想家联系人**/
	public static final int SearchShareHomeContact2 = 0002;
	/**点拨号的次数**/
	public static int CallSize = 0;
	/**重新登录**/
	public static final int RESTART_RELOGIN = 2359773;

	/**
	 * 通话类型
	 * 
	 */
	public static final String CALLRECORDER_TYPE_VIDEO_MISSED = "0";
	public static final String CALLRECORDER_TYPE_VIDEO_RECEIVED = "1";
	public static final String CALLRECORDER_TYPE_VIDEO_DIAL = "2";
	public static final String CALLRECORDER_TYPE_AUDIO_MISSED = "3";
	public static final String CALLRECORDER_TYPE_AUDIO_RECEIVED = "4";
	public static final String CALLRECORDER_TYPE_AUDIO_DIAL = "5";
	/**
	 * 广播IntentFilter 新的朋友
	 */
	public static final String NEW_FRIENDS_STATUS_CHAGED = "com.vunke.sharehome.Config.NEW_FRIENDS_STATUS_CHANGED";
	/**
	 * 广播IntentFilter 拨号按钮样式
	 */
	public static final String CALL_DAIL="com.vunke.sharehome.Config.CALL_DAIL";
	
}
