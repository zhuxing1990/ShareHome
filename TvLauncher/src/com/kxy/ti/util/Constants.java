package com.kxy.ti.util;

public class Constants {
	
	
	/**
	 * 用户注册广播
	 */
	public final static String REGISTER_USER_ACTION="android.bellmann.REGISTER_USER_INFO";
	
	/**
	 * 节目播放的广播
	 */
	public final static String ADVERTISING_ACTION = "android.bellmann.ADVERTISING";

	/**
	 * 向机顶盒发送读取机顶盒相关配置信息广播
	 */
	public final static String REQUEST_USER_INFO_ACTION = "android.bellmann.REQUEST_USER_INFO";

	/**
	 * 监听读取机顶盒相关配置信息广播
	 */
	public final static String LOAD_USER_INFO_ACTION = "android.bellmann.LOAD_USER_INFO";

	/**
	 * 监听下发平台业务工单是否对匹配成功广播
	 */
	public final static String REGISTER_STATUS_ACTION = "android.bellmann.REGISTER_STATUS";

	/**
	 * 监听下发结果信息广播
	 */
	public final static String REGISTER_RESULT_ACTION = "android.bellmann.REGISTER_RESULT";

	/**
	 * 监听机顶盒回应SetParameterValuesResponse
	 */
	public final static String REGISTER_REBOOT_ACTION = "android.bellmann.REGISTER_REBOOT";
	
	public final static String ERROR_HELP = "请联系10000！";
	
	public final static String ADVERT_PALY_PATH="/atest2";
	
	public final static String ADVERT_HTTP_SAVE_PATH="/atest1/ftptest";
	
	public final static String ADVERT_READ_PATH="/atest2/ftptest";
	
	public final static String ADVERT_SAVE_PATH="/atest1";

}
