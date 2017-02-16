package com.vunke.sharehome.view;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vunke.sharehome.R;

/**
 * 自定义 DIALOG  
 *  取消按钮在右上方
 *  默认设置输入框输入类型为 Number 类型
 *  
 * @author zhuxi
 *
 */
public class InputDataDialog {
	private Context context;
	private Dialog dialog;
	private TextView txt_title;
	private Button txt_cancel;
	private Display display;
	private EditText edit_message;
	private boolean showTitle = false;
	private boolean edit_allow_null = false;
	private Button txtcommit;
	private Handler handler = new Handler();

	public InputDataDialog(Context context) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
	}

	public InputDataDialog builder() {
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_buytraffic, null);

		view.setMinimumWidth(display.getWidth());

		// sLayout_content = (ScrollView)
		// view.findViewById(R.id.sLayout_content);
		// lLayout_content = (LinearLayout) view
		// .findViewById(R.id.lLayout_content);
		txt_title = (TextView) view.findViewById(R.id.buytraffic_title);
		txt_cancel = (Button) view.findViewById(R.id.buytraffic_cance);
		txtcommit = (Button) view.findViewById(R.id.buytraffic_ok);
		edit_message = (EditText) view.findViewById(R.id.buytraffic_code);
		edit_message.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (edit_allow_null == false) {
					int status1 = !TextUtils.isEmpty(s) ? 1 : 0;
					txtcommit.setEnabled(status1 == 1 ? true : false);
					txtcommit
							.setBackgroundResource(status1 == 1 ? R.drawable.button_login_shape
									: R.drawable.button_login_shape2);
				}
			}
		});
		txt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
		dialog.setContentView(view);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);
		dialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				popupInputMethodWindow();
			}
		});
		return this;
	}

	/**
	 * 设置标题
	 * 
	 * @param title
	 * @return
	 */
	public InputDataDialog setTitle(String title) {
		showTitle = true;
		txt_title.setVisibility(View.VISIBLE);
		txt_title.setText(title);
		txt_title.setTextSize(16);
		return this;
	}

	/**
	 * 设置隐藏关闭
	 * 
	 * @param isVISIBLE
	 * @return
	 */
	public InputDataDialog setCancelVISIBLE(boolean isVISIBLE) {
		txt_cancel.setVisibility(isVISIBLE ? View.VISIBLE : View.GONE);
		return this;
	}

	/**
	 * 设置按返回键是否关闭 DIALOG
	 * 
	 * @param cancel
	 * @return
	 */
	public InputDataDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	/**
	 * 设置点击DIALOG外面 自动关闭DIALOG
	 * 
	 * @param cancel
	 * @return
	 */
	public InputDataDialog setCanceledOnTouchOutside(boolean cancel) {
		dialog.setCanceledOnTouchOutside(cancel);
		return this;
	}

	/**
	 * 设置DIALOG gravity
	 * 
	 * @param gravity
	 * @return
	 */
	public InputDataDialog setDialogGravity(int gravity) {
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(gravity);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);
		return this;
	}

	/**
	 * 设置输入框提示 hint
	 * 
	 * @param hint
	 * @return
	 */
	public InputDataDialog setHint(String hint) {
		edit_message.setHint(hint);
		return this;
	}

	/**
	 * 设置输入框提示 hint
	 * 
	 * @param hint
	 * @return
	 */
	public InputDataDialog setHint(int hint) {
		edit_message.setHint(hint);
		return this;
	}

	/**
	 * 设置 新的输入框发生改变后的方法
	 * 
	 * @param watcher
	 * @return
	 */
	public InputDataDialog AddEditTextChangedListener(TextWatcher watcher) {
		edit_message.addTextChangedListener(watcher);
		return this;
	}

	/**
	 * 设置输入框为空也可以点确定
	 * 
	 * @param isAllow_null
	 * @return
	 */
	public InputDataDialog setCommitAllow_null(boolean isAllow_null) {
		txtcommit.setEnabled(isAllow_null);
		txtcommit.setBackgroundResource(R.drawable.button_login_shape);
		edit_allow_null = true;
		return this;
	}

	/**
	 * 设置确定的点击事件
	 * 
	 * @param listener
	 * @return
	 */
	public InputDataDialog SetCommitOnClickListener(
			View.OnClickListener listener) {
		txtcommit.setOnClickListener(listener);
		return this;
	}

	/**
	 * 设置取消的点击事件
	 * 
	 * @param listener
	 * @return
	 */
	public InputDataDialog SetCancelOnClickListener(
			View.OnClickListener listener) {
		txt_cancel.setOnClickListener(listener);
		return this;
	}

	/**
	 * 获取输入框
	 * 
	 * @return
	 */
	public EditText getEditText() {
		return edit_message;
	}

	/**
	 * 获取确定按钮
	 * 
	 * @return
	 */
	public Button getCommit() {
		return txtcommit;
	}

	/**
	 * 获取取消按钮
	 * 
	 * @return
	 */
	public Button getCancel() {
		return txt_cancel;
	}

	/**
	 * 当输入框获取到焦点就弹出输入法
	 */
	public void popupInputMethodWindow() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) edit_message
						.getContext().getSystemService(
								Service.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 0);
	}

	/**
	 * 显示弹窗
	 */
	public void show() {
		dialog.show();
	}

	/**
	 * 销毁弹窗
	 */
	public void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
}
