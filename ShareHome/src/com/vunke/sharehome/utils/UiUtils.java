package com.vunke.sharehome.utils;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.huawei.rcs.contact.ContactApi;
import com.huawei.rcs.contact.ContactSummary;
import com.huawei.rcs.login.LoginApi;
import com.huawei.rcs.login.UserInfo;
import com.vunke.sharehome.Config;
import com.vunke.sharehome.R;
import com.vunke.sharehome.activity.LoginActivity2;
import com.vunke.sharehome.base.HuaweiSDKApplication;
import com.vunke.sharehome.greendao.dao.ContactDao.Properties;
import com.vunke.sharehome.greendao.dao.bean.CallRecorders;
import com.vunke.sharehome.greendao.dao.bean.Contact;
import com.vunke.sharehome.greendao.util.DbCore;
import com.vunke.sharehome.service.GrayService;
import com.vunke.sharehome.service.MyJobService;
import com.vunke.sharehome.updata.AppTVStoreUpdateManager;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

@SuppressLint("NewApi")
public class UiUtils {

	public static Context getContext() {
		Context context = HuaweiSDKApplication.getApplication();

		return context;
	}

	/**
	 * 吐司
	 * 
	 * @param string
	 */
	public static void showToast(String string) {
		Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
	}

	public static void UpdateAPK(Context mcontext) {
		if (NetUtils.isNetConnected(mcontext)) {
			long theDate = System.currentTimeMillis();
			long isSameToday = (long) SharedPreferencesUtils.get(mcontext,
					Config.UPDATE_TOMORROW, Config.defaultValue);
			boolean sameToday = UiUtils.isSameToday(theDate, isSameToday);
			if (sameToday) {
				WorkLog.e("UpdateManager", "theDate:" + theDate
						+ "\n isSameToday:" + isSameToday);
				WorkLog.e("UpdateManager", "暂不更新");
				return;
			} else {
				 WorkLog.e("UpdateManager", "theDate:" + theDate
				 + "\n isSameToday:" + isSameToday);
				 WorkLog.e("UpdateManager", "检测更新");
				AppTVStoreUpdateManager updateManager = new AppTVStoreUpdateManager(
						mcontext);
				updateManager.GetAppTVStoreUpdateInfo();
			}
		}
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}

	/**
	 * 中文识别
	 * 
	 * @author luman
	 */
	public static boolean hasChinese(String source) {
		String reg_charset = "([\\u4E00-\\u9FA5]*+)";
		Pattern p = Pattern.compile(reg_charset);
		Matcher m = p.matcher(source);
		boolean hasChinese = false;
		while (m.find()) {
			if (!"".equals(m.group(1))) {
				hasChinese = true;
			}

		}
		return hasChinese;
	}

	/**
	 * 过滤特殊字符
	 * 
	 * @param str
	 * @return boolean 有返回false 没有返回true
	 * @throws PatternSyntaxException
	 */
	public static boolean StringFilter(String str)
			throws PatternSyntaxException {
		String reg = "^([a-z]|[A-Z]|[0-9]|[\u2E80-\u9FFF]){3,}|@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?|[wap.]{4}|[www.]{4}|[blog.]{5}|[bbs.]{4}|[.com]{4}|[.cn]{3}|[.net]{4}|[.org]{4}|[http://]{7}|[ftp://]{6}$";
		Pattern p = Pattern.compile(reg);
		Matcher matcher = p.matcher(str);
		if (!matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean getReLogin(Context context) {
		boolean b = (boolean) SharedPreferencesUtils.get(context,
				Config.RELOGIN, false);
		return b;
	}

	@SuppressLint("NewApi")
	public static void StartJobScheduler(Context context) {
		try {
			JobScheduler scheduler = (JobScheduler) context
					.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			// JobInfo.Builder builder = new JobInfo.Builder( 1,
			// new ComponentName( context.getPackageName(),
			// MyJobService.class.getName() ) );
			ComponentName mServieComponent = new ComponentName(
					context.getPackageName(), MyJobService.class.getName());
			for (int i = 0; i < 10; i++) {
				JobInfo jobInfo = new JobInfo.Builder(i, mServieComponent)
				/*
				 * 这会使你的工作不启动直到规定的毫秒数已经过去了
				 */
				.setMinimumLatency(5000)
				/*
				 * 这将设置你的工作期限。即使是无法满足其他要求，你的任务将约在规定的时间已经过去时开始执行
				 */
				.setOverrideDeadline(60000)
				/*
				 * 只有在设备处于一种特定的网络中时，它才启动 默认值是JobInfo.NETWORK_TYPE_NONE
				 * :无论是否有网络连接，该任务均可以运行
				 * JobInfo.NETWORK_TYPE_ANY，这需要某种类型的网络连接可用，工作才可以运行
				 * JobInfo.NETWORK_TYPE_UNMETERED，这就要求设备在非蜂窝网络中
				 */
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build();
				WorkLog.e("schedule job ", i + " !\n");
				scheduler.schedule(jobInfo);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断手机是否有SD卡。
	 * 
	 * @return 有SD卡返回true，没有返回false。
	 */
	public static boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * 正则表达式 判断手机号 13[0-9], 14[4,5,7], 15[0-9], 17[6, 7, 8], 18[0-9], 170[0-9]
	 * 移动号段：139 138 137 136 135 134 147 150 151 152 157 158 159 178 182 183 184
	 * 187 188 联通号段：130 131 132 155 156 185 186 145 176 电信号段：133 153 177 173 180
	 * 181 189 虚拟运营商号段：170 171
	 */
	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern
		// .compile("^1(3[0-9]|4[0-9]|5[0-9]|7[0-9]|8[0-9])\\d{8}$");
				.compile("^1\\d{10}$");
		// .compile("^((1[358][0-9])|(14[57])|(17[0678]))\\d{8}$");
		// .compile("^((1[35789][0-9])|(14[457]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		// System.out.println(m.matches() + "---");
		return m.matches();
	}

	/**
	 * 正则表达式 判断拨打的号码格式是否正确
	 * */
	public static boolean isCallNumber(String CallNumber) {
		Pattern p = Pattern
				.compile("^11831726[89](1(3[0-9]|4[0-9]|5[0-9]|7[0-9]|8[0-9])\\d{8}$)");
		Matcher m = p.matcher(CallNumber);
		// System.out.println(m.matches() + "isCallNumber");
		return m.matches();
	}

	/**
	 * 隐藏控件 动画
	 * */
	public static void showAnim_in(View... view) {
		Animation animation = AnimationUtils.loadAnimation(getContext(),
				R.anim.push_bottom_in);
		for (int i = 0; i < view.length; i++) {
			view[i].setAnimation(animation);
		}
	}

	/**
	 * 正则表达式 判断拨号Code是否正确
	 * */
	public static boolean isCallCode(String CallCode) {
		if (CallCode.startsWith("11831726")) {
			// System.out.println(true);
			return true;
		}
		return false;
	}

	/**
	 * 正则表达式 判断密码
	 */
	public static boolean isPasswordStandard(String user_wd) {

		// 不能包含中文
		if (hasChinese(user_wd)) {
			return false;
		}

		/**
		 * 正则匹配 \\w{6,18}匹配所有字母、数字、字符串长度6
		 */
		// String format = "[0-9a-zA-Z]{6,}$";(@?+\\w){6,18}+
		String format = "(@?+\\w){6,}+";
		if (user_wd.matches(format)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断通话记录的权限
	 * */
	public static boolean isCallLogCode(Context context) {
		PackageManager pm = context.getPackageManager();
		boolean permission = (PackageManager.PERMISSION_GRANTED == pm
				.checkPermission("android.permission.READ_CALL_LOG",
						"com.vunke.sharehome"));
		if (permission) {
			return true;
		}
		return false;
	}

	/**
	 * 显示控件 动画
	 * */
	public static void showAnim_out(View... view) {
		Animation animation = AnimationUtils.loadAnimation(getContext(),
				R.anim.push_bottom_out);
		for (int i = 0; i < view.length; i++) {
			view[i].setAnimation(animation);
		}
	}

	/**
	 * 通过电话号码 查询 greenDao 数据库
	 * */
	public static List<Contact> SearchContact(String number) {
		Query<Contact> query = DbCore.getDaoSession().getContactDao()
				.queryBuilder().where(Properties.HomePhone.eq(number)).build();
		List<Contact> contact = query.list();
		return contact;
	}

	public static List<Contact> SearchContact(Long pid) {
		Query<Contact> query = DbCore.getDaoSession().getContactDao()
				.queryBuilder().where(Properties.UserId.eq(pid)).build();
		List<Contact> contact = query.list();
		return contact;
	}

	/**
	 * 插入通话记录
	 * */
	public static long InsertCallLog(String phone, String calType,
			String call_time) {
		// WorkLog.e("UiUtils", "开始插入通话记录");
		String phone2;
		// WorkLog.e("UiUtils", "call_time" + call_time + "calType" + calType +
		// "phone"
		// + phone);
		// if (phone.startsWith("8") || phone.startsWith("9")) {
		// phone2 = phone.substring(1);
		// } else {
		// phone2 = phone;
		// }
		phone2 = isMobileNO(phone) ? phone : phone.substring(1);
		List<Contact> searchContact = SearchContact(phone2);
		Contact contact = null;
		if (searchContact != null && searchContact.size() != 0) {
			for (int i = 0; i < searchContact.size(); i++) {
				contact = searchContact.get(i);
			}
		}
		CallRecorders callRecorders = new CallRecorders();
		try {
			if (contact != null) {
				callRecorders.setContactName(contact.getContactName());
			} else {
				callRecorders.setContactName("未知号码");
				List<ContactSummary> contactSummaries = ContactApi
						.searchContact(phone2, ContactApi.LIST_FILTER_ALL);
				if (contactSummaries.size() != 0 && contactSummaries != null) {
					for (int i = 0; i < contactSummaries.size(); i++) {
						ContactSummary position = contactSummaries.get(i);
						if (phone2.equals(position.getSearchMatchContent())) {
							callRecorders.setContactName(position
									.getDisplayName());
						}
					}
					// if (contactSummaries.size() == 1) {
					// callRecorders.setContactName(contactSummaries
					// .get(contactSummaries.size() - 1)
					// .getDisplayName().toString());
					// }
				}
			}
			callRecorders.setCallRecordersPhone(phone);
			callRecorders.setCallType(calType);
			callRecorders.setCreateTime(new Date());
			if (!TextUtils.isEmpty(call_time)) {
				callRecorders.setCallTime(call_time);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DbCore.getDaoSession().getCallRecordersDao()
				.insert(callRecorders);
	}

	/**
	 * 使用系统工具类判断是否是今天 是今天就显示发送的小时分钟 不是今天就显示发送的那一天
	 * 
	 * @param when
	 *            时间 long 类型
	 * @return 如果是今天 返回 上中下午
	 */
	public static String getDate(long when) {
		String date = null;
		if (DateUtils.isToday(when)) {
			date = DateFormat.getTimeFormat(getContext()).format(when);
		} else {
			date = DateFormat.getDateFormat(getContext()).format(when);
		}
		return date;
	}

	/**
	 * 通过查找搜索内容 // User_name or Home_phone
	 */
	public static List<Contact> FuzzyQuery(String content) {

		QueryBuilder<Contact> queryBuilder = DbCore.getDaoSession()
				.getContactDao().queryBuilder();
		// DbCore.getDaoMaster().getDatabase().query(table, columns, selection,
		// selectionArgs, groupBy, having, orderBy)
		QueryBuilder.LOG_SQL = true;
		QueryBuilder.LOG_VALUES = true;
		/**
		 * select * from contact where user_name like '%q%' or home_phone like
		 * '%9%'
		 */
		queryBuilder.whereOr(Properties.ContactName.like("%" + content + "%"),
				Properties.HomePhone.like("%" + content + "%"));
		queryBuilder.orderDesc(Properties.UserId);
		List<Contact> contactList = queryBuilder.build().list();

		return contactList;
	}

	/**
	 * 精确查询
	 * 
	 * @param ContactName
	 *            姓名
	 * @param HomePhone
	 *            手机号码
	 * @return
	 */
	public static List<Contact> PreciseQuery(String ContactName,
			String HomePhone) {
		QueryBuilder<Contact> queryBuilder = DbCore.getDaoSession()
				.getContactDao().queryBuilder();
		QueryBuilder.LOG_SQL = true;
		QueryBuilder.LOG_VALUES = true;
		/**
		 * select * from contact where user_name like '%q%' or home_phone like
		 * '%9%'
		 */
		// queryBuilder.whereOr(
		// Properties.ContactName.like("%" + ContactName + "%"),
		// Properties.HomePhone.like("%" + HomePhone + "%"));
		// queryBuilder.orderDesc(Properties.UserId);
		queryBuilder.whereOr(Properties.ContactName.eq(ContactName),
				Properties.HomePhone.eq(HomePhone));
		queryBuilder.orderDesc(Properties.UserId);
		List<Contact> contactList = queryBuilder.build().list();

		return contactList;
	}

	/**
	 * @param phone
	 *            电话号码
	 * @return 去掉 11831726 和 8/9 的前缀
	 */
	public static String initCallNumber(String phone) {
		String phone2;
		if (phone.length() >= 9) {// 电话号码的长度值大于9
			// 如果电话号码包含了 11831726加8或9
			if (phone.contains(Config.CALL_BEFORE + "8")
					|| phone.contains(Config.CALL_BEFORE + "9")) {
				phone2 = phone.substring(9, phone.length());
				// WorkLog.e("UiUtils", "去前缀后:" + phone2);
				return phone2;
			} else {
				phone2 = phone.substring(8, phone.length());
				// WorkLog.e("UiUtils", "去前缀后:" + phone2);
				return phone2;
			}
		} else {
			// WorkLog.e("UiUtils", "去前缀后:" + phone);
			return phone;
		}
	}

	/**
	 * @param phone
	 *            电话号码
	 * @return 去掉 11831726的前缀
	 */
	public static String initCallNumber2(String phone) {
		String phone2;
		// WorkLog.e("UiUtils", "拨号传过来的号码:" + phone);//11831726+ 8/9 + 手机号码

		if (phone.length() >= 9) {// 电话号码的长度值大于9
			// 如果电话号码包含了 11831726加8或9
			if (phone.contains(Config.CALL_BEFORE + "8")
					|| phone.contains(Config.CALL_BEFORE + "9")) {
				phone2 = phone.substring(8, phone.length());
				// WorkLog.e("UiUtils", "包含去前缀后:" + phone2);
				return phone2;
			} else {
				phone2 = phone.substring(8, phone.length());
				// WorkLog.e("UiUtils", "不包含去前缀后:" + phone2);
				return phone2;
			}
		} else {
			// WorkLog.e("UiUtils", "去前缀后:" + phone);
			return phone;
		}
	}

	/**
	 * 删除输入框最后的一个字符
	 * */
	public static void ClearNumber(EditText editText) {
		String string = editText.getText().toString().trim();
		if (string.length() == 0) {

		} else if (string.length() > 0) {
			int index = editText.getSelectionStart();
			if (index != 0) {
				Editable editable = editText.getText();
				editable.delete(index - 1, index);
			}
		}
	}

	/**
	 * 删除输入框最后的一个字符2
	 * */
	public static void ClearNumber2(EditText editText) {
		String clearText = editText.getText().toString().trim();
		if (clearText.length() == 0) {

		} else if (clearText.length() > 0) {
			StringBuffer buffer = new StringBuffer(clearText);
			buffer.deleteCharAt(buffer.length() - 1);
			editText.setText(buffer.toString().trim());
		}
	}

	/**
	 * 根据用户帐号获取TV帐号
	 * */
	public static String GetTVUserName() {
		String tvUser = "8" + UiUtils.GetUserName().substring(1);// 根据登录帐号返回
		return tvUser;
	}

	/**
	 * 获取用户帐号
	 * */
	public static String GetUserName() {
		UserInfo mLastUserInfo = LoginApi.getUserInfo(LoginApi
				.getLastUserName());
		String Name = null;
		if (mLastUserInfo != null) {
			if (!TextUtils.isEmpty(mLastUserInfo.countryCode)) {// 自动写入帐号
				if ((null != mLastUserInfo)
						&& (null != mLastUserInfo.countryCode)
						&& (null != mLastUserInfo.username)) {

					if (mLastUserInfo.username.startsWith("+")) {
						int length = mLastUserInfo.countryCode.length();
						if (!mLastUserInfo.countryCode.startsWith("+")) {
							length++;
						}
						String userName = mLastUserInfo.username
								.substring(length);
						if (userName.contains(Config.CALL_BEFORE)) {
							userName = userName.substring(8, userName.length());
						}

						Name = userName;
					} else {
						if (mLastUserInfo.username.contains(Config.CALL_BEFORE)) {
							mLastUserInfo.username = mLastUserInfo.username
									.substring(8,
											mLastUserInfo.username.length());
						}
						Name = mLastUserInfo.username;

					}
				}
			}
		}
		if (TextUtils.isEmpty(Name)) {
			return "null";
		}
		return Name;
	}

	/**
	 * @param context
	 *            手机看家
	 */
	public static void setLookHome(Context context) {
		SharedPreferences sp = context.getSharedPreferences(Config.UserName,
				context.MODE_PRIVATE);
		String TVUserName = GetTVUserName();// 登录的号码
		boolean HasUserName = sp.getBoolean(Config.HasUserName, true);
		if (HasUserName) {
			Contact addcontact = new Contact();
			addcontact.setContactName("手机看家");
			addcontact.setHomePhone(TVUserName);
			addcontact.setGroupId("2");
			DbCore.getDaoSession().getContactDao().insert(addcontact);
			List<Contact> all = DbCore.getDaoSession().getContactDao()
					.loadAll();
			Editor editor = sp.edit();
			if (all.size() != 0 && all != null) {
				if (all.size() == 1) {
					Long TVUserId = all.get(0).getUserId();
					editor.putLong(Config.LOOKHOME, TVUserId);
				} else if (all.size() > 1) {
					Long TVUserId = all.get(all.size() - 1).getUserId();
					editor.putLong(Config.LOOKHOME, TVUserId);
				} else {

				}
			}
			editor.putBoolean(Config.HasUserName, false);
			editor.commit();
		}
	}

	/**
	 * 过滤表情 emoji表情字符串 mysql utf8数据库无法save.
	 * */
	public static String filterEmoji(String source) {
		if (source != null) {
			Pattern emoji = Pattern
					.compile(
							"[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
							Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
			Matcher emojiMatcher = emoji.matcher(source);
			if (emojiMatcher.find()) {
				source = emojiMatcher.replaceAll("*");
				return source;
			}
			return source;
		}
		return source;
	}

	/**
	 * @param context
	 * @return versionName 版本名字
	 */
	public static String getVersionName(Context context) {
		String versionName = "";
		try {
			versionName = context.getPackageManager().getPackageInfo(
					"com.vunke.sharehome", 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
		return versionName;
	}

	/**
	 * @param context
	 * @return versionCode 版本号
	 */
	public static int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(
					"com.vunke.sharehome", 0).versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return versionCode;
	}

	/**
	 * 打电话
	 * 
	 * @param tel
	 *            电话号码
	 * @param context
	 *            上下文
	 * @param Type
	 *            拨打类型 1拨号键盘 2直接波
	 */
	public static void CallUserPhone(String tel, Context context, int Type) {
		Uri uri = Uri.parse("tel:" + tel);
		switch (Type) {
		case 1:
			// 进入拨号界面
			Config.intent = new Intent(Intent.ACTION_DIAL, uri);
			Config.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(Config.intent);
			break;
		case 2:
			// 直接拨打
			Config.intent = new Intent(Intent.ACTION_CALL, uri);
			Config.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(Config.intent);
			break;

		default:
			break;
		}
	}

	/*****
	 * 把登陆信息存入cookie
	 * 
	 * ********/
	// 写入cookie
	public static void synCookies(Context context, String url, JSONObject data) {
		// removeCookie(context);
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setCookie(url, "CookieData=" + data);
		CookieSyncManager.getInstance().sync();
	}

	// 删除cookie
	public static void removeCookie(Context context) {
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		CookieSyncManager.getInstance().sync();
	}

	/**
	 * 获取当前时间
	 * 
	 * @return String 2016-6-12 10:53:05:888
	 */
	public static String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss:SS");
		Date date = new Date(System.currentTimeMillis());
		String time = dateFormat.format(date);
		return time;
	}

	/**
	 * 测试当前摄像头能否被使用
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public static boolean isCameraCanUse() {
		boolean canUse = true;
		Camera mCamera = null;
		try {
			mCamera = Camera.open(0);
			mCamera.setDisplayOrientation(90);
		} catch (Exception e) {
			canUse = false;
		}
		if (canUse) {
			mCamera.release();
			mCamera = null;
		}
		// WorkLog.e("UiUtils", "isCameraCanuse="+canUse);
		return canUse;
	}

	/** Check if this device has a camera */

	private static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;

		} else {
			// no camera on this device
			return false;
		}
	}

	private static long lastClickTime = 0;

	/**
	 * 防止按钮重复点击
	 * 
	 * @param ts
	 * @return
	 */
	public static boolean isFastDoubleClick(float ts) {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		lastClickTime = time;
		if (0 < timeD && timeD < ts * 1000) {
			return true;
		}
		return false;
	}

	/**
	 * 设置网络
	 * 
	 * @param paramContext
	 */
	public static void startToSettings(Context paramContext) {
		if (paramContext == null)
			return;
		try {
			if (Build.VERSION.SDK_INT > 10) {
				paramContext.startActivity(new Intent(
						"android.settings.SETTINGS"));
				return;
			}
		} catch (Exception localException) {
			localException.printStackTrace();
			return;
		}
		paramContext.startActivity(new Intent(
				"android.settings.WIRELESS_SETTINGS"));
	}

	/**
	 * 根据两个长整形数，判断是否是同一天
	 * 
	 * @param lastDay
	 * @param thisDay
	 * @return
	 */
	public static boolean isSameToday(long lastDay, long thisDay) {
		Time time = new Time();
		time.set(lastDay);

		int thenYear = time.year;
		int thenMonth = time.month;
		int thenMonthDay = time.monthDay;

		time.set(thisDay);
		return (thenYear == time.year) && (thenMonth == time.month)
				&& (thenMonthDay == time.monthDay);
	}

	/**
	 * 使用系统工具类判断是否是今天 是今天就显示发送的小时分钟 不是今天就显示发送的那一天
	 * */
	public static String getDate(Context context, long when) {
		String date = null;
		if (DateUtils.isToday(when)) {
			date = DateFormat.getTimeFormat(context).format(when);
		} else {
			date = DateFormat.getDateFormat(context).format(when);
		}
		return date;
	}
}
